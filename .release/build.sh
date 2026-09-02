#!/usr/bin/env bash
set -euo pipefail

release_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
project_dir=$(cd "${release_dir}/.." && pwd)
source "${release_dir}/project.env"

out_dir="${release_dir}/out"
work_dir="${release_dir}/work"
dependency_bundle_dir="${work_dir}/dependency-bundle"
application_bundle_dir="${work_dir}/application-bundle"
layer_extract_dir="${work_dir}/java-layers"
rm -rf "${out_dir}" "${work_dir}"
mkdir -p "${out_dir}" "${dependency_bundle_dir}/java-runtime" \
  "${application_bundle_dir}/java-runtime" "${layer_extract_dir}"

cd "${project_dir}"
maven_command=(mvn)
[[ -x ./mvnw ]] && maven_command=(./mvnw)
"${maven_command[@]}" -B -DskipTests package

(
  cd ui-admin-arco
  pnpm install --frozen-lockfile
  pnpm run build
)

artifact=${JAVA_ARTIFACT:-}
if [[ -z ${artifact} ]]; then
  candidates=()
  while IFS= read -r candidate; do
    candidates+=("${candidate}")
  done < <(find . -path '*/target/*.jar' -type f \
    ! -name '*-sources.jar' ! -name '*-javadoc.jar' ! -name 'original-*.jar' | sort)
  [[ ${#candidates[@]} -eq 1 ]] \
    || { echo "Expected one deployable JAR, found ${#candidates[@]}. Set JAVA_ARTIFACT." >&2; exit 1; }
  artifact=${candidates[0]}
fi
[[ -f ${artifact} ]] || { echo "Java artifact does not exist: ${artifact}" >&2; exit 1; }

java_bin=${JAVA_HOME:+${JAVA_HOME}/bin/java}
java_bin=${java_bin:-$(command -v java)}
"${java_bin}" -Djarmode=tools -jar "${artifact}" extract --layers --destination "${layer_extract_dir}"
for layer in dependencies snapshot-dependencies application; do
  [[ -d ${layer_extract_dir}/${layer} ]] || { echo "Missing Spring Boot layer: ${layer}" >&2; exit 1; }
done

cp -R "${layer_extract_dir}/dependencies" "${dependency_bundle_dir}/java-runtime/dependencies"
cp -R "${layer_extract_dir}/snapshot-dependencies" "${application_bundle_dir}/java-runtime/snapshot-dependencies"
cp -R "${layer_extract_dir}/application" "${application_bundle_dir}/java-runtime/application"
application_jars=("${application_bundle_dir}/java-runtime/application"/*.jar)
[[ ${#application_jars[@]} -eq 1 && -f ${application_jars[0]} ]] \
  || { echo "Expected one Spring Boot application layer JAR." >&2; exit 1; }
mv "${application_jars[0]}" "${application_bundle_dir}/java-runtime/application/app.jar"
start_class=$(unzip -p "${artifact}" META-INF/MANIFEST.MF | sed -n 's/^Start-Class: //p' | tr -d '\r' | head -1)
[[ ${start_class} =~ ^[A-Za-z_$][A-Za-z0-9_$.]*$ ]] \
  || { echo "Invalid or missing Spring Boot Start-Class." >&2; exit 1; }
printf '%s\n' "${start_class}" > "${application_bundle_dir}/java-runtime/start-class"

install -d "${application_bundle_dir}/ui-admin-arco"
cp -R "${project_dir}/ui-admin-arco/dist" "${application_bundle_dir}/ui-admin-arco/dist"

copy_runtime_path() {
  local path=$1
  [[ ${path} != /* && ${path} != *..* && -e ${path} ]] \
    || { echo "Invalid or missing runtime path: ${path}" >&2; exit 1; }
  mkdir -p "${application_bundle_dir}/$(dirname "${path}")"
  cp -R "${path}" "${application_bundle_dir}/${path}"
}
while IFS= read -r path || [[ -n ${path} ]]; do
  path=${path%%#*}
  path=$(printf '%s' "${path}" | tr -d '[:space:]')
  [[ -z ${path} ]] || copy_runtime_path "${path}"
done < "${SOURCE_MANIFEST:-.release/source-manifest.txt}"
for path in ${RELEASE_REQUIRED_FILES:-}; do
  copy_runtime_path "${path}"
done

cat > "${application_bundle_dir}/run.sh" <<'RUN_SCRIPT'
#!/usr/bin/env bash
set -euo pipefail
app_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
runtime_dir="${app_dir}/java-runtime"
start_class=$(<"${runtime_dir}/start-class")
[[ ${start_class} =~ ^[A-Za-z_$][A-Za-z0-9_$.]*$ ]] || exit 78
classpath="${runtime_dir}/application/app.jar:${runtime_dir}/application/lib/*:${runtime_dir}/dependencies/lib/*:${runtime_dir}/snapshot-dependencies/lib/*"
exec /usr/bin/java ${JAVA_OPTS:-} -cp "${classpath}" "${start_class}"
RUN_SCRIPT
chmod 0755 "${application_bundle_dir}/run.sh"

manifest_tmp=$(mktemp)
{
  find "${dependency_bundle_dir}" -type f -print | sed "s#^${dependency_bundle_dir}/##"
  find "${application_bundle_dir}" -type f -print | sed "s#^${application_bundle_dir}/##"
} | LC_ALL=C sort > "${manifest_tmp}"
install -m 0644 "${manifest_tmp}" "${application_bundle_dir}/.release-manifest"
rm -f "${manifest_tmp}"

"${release_dir}/assemble.sh" components \
  "${dependency_bundle_dir}" "${application_bundle_dir}" "${out_dir}"

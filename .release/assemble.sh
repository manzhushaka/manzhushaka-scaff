#!/usr/bin/env bash
set -euo pipefail

mode=${1:-}
first=${2:-}
second=${3:-}
out_dir=${4:-}
cleanup_extracted_layers=false

create_archive() {
  local source_dir=$1 archive=$2
  find "${source_dir}" -type f -exec touch -t 198001010000 {} +
  if tar --version 2>&1 | grep -q 'GNU tar'; then
    (
      cd "${source_dir}"
      find . -type f -print | LC_ALL=C sort \
        | tar --format=ustar --mtime='1980-01-01 UTC' --owner=0 --group=0 --numeric-owner -cf - -T -
    ) | gzip -n > "${archive}"
  else
    (
      cd "${source_dir}"
      export COPYFILE_DISABLE=1
      find . -type f -print | LC_ALL=C sort \
        | tar --format ustar --uid 0 --gid 0 --uname root --gname root --no-xattrs -cf - -T -
    ) | gzip -n > "${archive}"
  fi
}

write_checksum() {
  local archive=$1
  (cd "$(dirname "${archive}")" && sha256sum "$(basename "${archive}")" > "$(basename "${archive}").sha256")
}

verify_checksum() {
  local archive=$1
  (cd "$(dirname "${archive}")" && sha256sum -c "$(basename "${archive}").sha256")
}

write_manifest() {
  local archive=$1 manifest=$2 raw_manifest
  raw_manifest=$(mktemp)
  tar -tzf "${archive}" > "${raw_manifest}"
  sed 's#^\./##' "${raw_manifest}" | LC_ALL=C sort > "${manifest}"
  rm -f "${raw_manifest}"
  (cd "$(dirname "${manifest}")" && sha256sum "$(basename "${manifest}")" > "$(basename "${manifest}").sha256")
}

case "${mode}" in
  components)
    dependency_dir=${first}
    application_dir=${second}
    [[ -d ${dependency_dir} && -d ${application_dir} && -n ${out_dir} ]] \
      || { echo "Usage: $0 components DEPENDENCY_DIR APPLICATION_DIR OUT_DIR" >&2; exit 1; }
    mkdir -p "${out_dir}"
    create_archive "${dependency_dir}" "${out_dir}/dependencies.tar.gz"
    create_archive "${application_dir}" "${out_dir}/application.tar.gz"
    write_checksum "${out_dir}/dependencies.tar.gz"
    write_checksum "${out_dir}/application.tar.gz"
    ;;
  archives)
    dependency_archive=${first}
    application_archive=${second}
    [[ -f ${dependency_archive} && -f ${dependency_archive}.sha256 \
      && -f ${application_archive} && -f ${application_archive}.sha256 && -n ${out_dir} ]] \
      || { echo "Usage: $0 archives DEPENDENCY_ARCHIVE APPLICATION_ARCHIVE OUT_DIR" >&2; exit 1; }
    verify_checksum "${dependency_archive}"
    verify_checksum "${application_archive}"
    dependency_dir=$(mktemp -d)
    application_dir=$(mktemp -d)
    cleanup_extracted_layers=true
    tar -xzf "${dependency_archive}" -C "${dependency_dir}"
    tar -xzf "${application_archive}" -C "${application_dir}"
    ;;
  *)
    echo "Usage: $0 components DEPENDENCY_DIR APPLICATION_DIR OUT_DIR | archives DEPENDENCY_ARCHIVE APPLICATION_ARCHIVE OUT_DIR" >&2
    exit 1
    ;;
esac

assembled_dir=$(mktemp -d)
cleanup() {
  rm -rf "${assembled_dir}"
  if [[ ${cleanup_extracted_layers} == true ]]; then
    rm -rf "${dependency_dir}" "${application_dir}"
  fi
}
trap cleanup EXIT
cp -R "${dependency_dir}/." "${assembled_dir}/"
cp -R "${application_dir}/." "${assembled_dir}/"
create_archive "${assembled_dir}" "${out_dir}/release.tar.gz"
write_checksum "${out_dir}/release.tar.gz"
write_manifest "${out_dir}/release.tar.gz" "${out_dir}/release-manifest.txt"

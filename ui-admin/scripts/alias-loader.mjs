import { existsSync } from 'node:fs';
import { dirname, extname, resolve as resolvePath } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const projectRoot = resolvePath(import.meta.dirname, '..');

export async function resolve(specifier, context, nextResolve) {
  if (specifier.startsWith('@/')) {
    const relativePath = specifier.slice(2);
    const withExtension = relativePath.endsWith('.ts') ? relativePath : `${relativePath}.ts`;
    return {
      shortCircuit: true,
      url: pathToFileURL(resolvePath(projectRoot, 'src', withExtension)).href,
    };
  }

  if ((specifier.startsWith('./') || specifier.startsWith('../')) && !extname(specifier) && context.parentURL) {
    const parentPath = dirname(fileURLToPath(context.parentURL));
    const candidate = resolvePath(parentPath, `${specifier}.ts`);
    if (existsSync(candidate)) {
      return {
        shortCircuit: true,
        url: pathToFileURL(candidate).href,
      };
    }
  }

  return nextResolve(specifier, context);
}

import { pathToFileURL } from 'node:url';
import { resolve as resolvePath } from 'node:path';

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

  return nextResolve(specifier, context);
}

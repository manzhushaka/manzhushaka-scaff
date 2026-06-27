import autoImport from 'unplugin-auto-import/vite'

export default function createAutoImport() {
  return autoImport({
    imports: [
      'vue',
      'vue-router',
      'pinia',
      {
        '@/utils/dict': ['useDict'],
        '@/utils/manzhushaka': ['selectDictLabel']
      }
    ],
    dts: false
  })
}

import { cp, mkdir, rm, readFile, writeFile } from 'node:fs/promises';
await rm('dist',{recursive:true,force:true});await mkdir('dist');await cp('public','dist',{recursive:true});
for (const file of ['index.html','app.js','core.js','styles.css','manifest.webmanifest','sw.js']) await cp(file,`dist/${file}`);
console.log('Static production build ready: dist/');

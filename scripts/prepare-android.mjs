import {cp,mkdir,rm,readFile,writeFile} from 'node:fs/promises';
const target='android/app/src/main/assets/site';
await rm(target,{recursive:true,force:true});await mkdir(target,{recursive:true});await cp('dist',target,{recursive:true});
// The Android build needs no external fonts or service worker: all files are in the APK.
const css=await readFile(`${target}/styles.css`,'utf8');await writeFile(`${target}/styles.css`,css.replace(/^@import[^\r\n]*[\r\n]+/m,''));
console.log('Android assets bundled from dist (data, graphics and app).');

"""Import the pinned GPL-3.0 Toolbox graph and referenced fan artwork. No account data."""
import json, shutil, subprocess, sys, hashlib
from pathlib import Path
from collections import Counter
root=Path(__file__).resolve().parents[1]
src=Path(sys.argv[1])
revision=subprocess.check_output(['git','-C',str(src),'rev-parse','HEAD'],text=True).strip()
graph=json.loads((src/'data/entity-graph.json').read_text())
out=root/'public';(out/'data').mkdir(parents=True,exist_ok=True)
assets=set()
def visit(v):
 if isinstance(v,dict):
  for x in v.values(): visit(x)
 elif isinstance(v,list):
  for x in v:visit(x)
 elif isinstance(v,str) and v.startswith('/') and '..' not in v.split('/') and v.endswith(('.png','.gif','.webp')):assets.add(v)
visit(graph)
assets.update(['/monsters/mushG/idle.gif','/etc/bg_0.png'])
missing=[];manifest=[]
for asset in sorted(assets):
 source=src/'public'/asset.lstrip('/')
 if not source.is_file(): missing.append(asset);continue
 target=out/'assets'/asset.lstrip('/');target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(source,target)
 manifest.append({'path':str(target.relative_to(out)), 'source':f'https://github.com/Morta1/IdleonToolbox/blob/{revision}/public{asset}', 'sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'rights':'IdleOn game artwork: LavaFlame2; fan reference use, not covered by app license.'})
(out/'data/graph.json').write_text(json.dumps(graph,separators=(',',':')))
# Preserve all structured game tables (NOT raw.json, user saves, fixtures or credentials).
archive=out/'data/tables';archive.mkdir(exist_ok=True)
for f in (src/'data/website-data').glob('*.json'):shutil.copy2(f,archive/f.name)
shutil.copy2(src/'LICENSE',root/'LICENSE')
meta={'retrieved':'2026-11-08','source':'https://github.com/Morta1/IdleonToolbox','revision':revision,'upstreamCommitDate':subprocess.check_output(['git','-C',str(src),'show','-s','--format=%cI'],text=True).strip(),'nodes':len(graph['nodes']),'edges':len(graph['edges']),'categories':dict(Counter(n['kind'] for n in graph['nodes'].values())),'assets':len(manifest),'missingAssets':missing,'tables':[p.name for p in sorted(archive.glob('*.json'))],'verification':'Imported community snapshot; not independently game-tested. Includes internal/non-catalogue records. Not a claim of full game coverage.'}
(out/'data/provenance.json').write_text(json.dumps(meta,indent=2))
(out/'data/asset-manifest.json').write_text(json.dumps(manifest,separators=(',',':')))
print(json.dumps({k:meta[k] for k in ['nodes','edges','assets','missingAssets']}))

import shutil
from pathlib import Path
from typing import Dict, List
import utils


class AssetManager:
    
    def __init__(self, project_root: Path, modid: str):
        self.project_root = project_root
        self.modid = modid
        self.source_assets = project_root / "assets"
        self.target_assets = project_root / "src/main/resources/assets" / modid
        
    def copy_textures(self, items: List[Dict], blocks: List[Dict]):
        print("\nCopying textures...")
        
        for item in items:
            texture = item.get('texture', item['id'])
            if not texture.endswith('.png'):
                texture += '.png'
            
            src = self.source_assets / "textures/item" / texture
            dest = self.target_assets / "textures/item" / texture
            self._copy_asset(src, dest)
        
        for block in blocks:
            textures = block.get('textures', {})
            if isinstance(textures, str):
                textures = {'all': textures}
            
            for tex_face, tex_file in textures.items():
                if not tex_file.endswith('.png'):
                    tex_file += '.png'
                
                src = self.source_assets / "textures/block" / tex_file
                dest = self.target_assets / "textures/block" / tex_file
                self._copy_asset(src, dest)
                item_filename = Path(tex_file).name
                item_src = self.source_assets / "textures/block" / tex_file
                item_dest = self.target_assets / "textures/item" / item_filename
                self._copy_asset(item_src, item_dest)
    
    def copy_sounds(self, items: List[Dict], blocks: List[Dict]):
        print("\nCopying sounds...")
        
        sound_refs = set()
        
        for item in items:
            if 'sound' in item:
                sound_refs.add(item['sound'])
        
        for block in blocks:
            if 'sound' in block:
                sound_refs.add(block['sound'])
        
        for sound_ref in sound_refs:
            sound_file = f"{sound_ref}.ogg"
            src = self.source_assets / "sounds" / sound_file
            dest = self.target_assets / "sounds" / sound_file
            self._copy_asset(src, dest)
    
    def _copy_asset(self, src: Path, dest: Path):
        if not src.exists():
            print(f"  [WARN] Source not found: {src}")
            return
        
        utils.ensure_dir(dest.parent)
        shutil.copy2(src, dest)
        print(f"  [COPY] {src.name} → {dest.relative_to(self.project_root)}")

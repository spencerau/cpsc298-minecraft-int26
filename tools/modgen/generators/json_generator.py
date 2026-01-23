import json
import shutil
from pathlib import Path
from typing import Dict, List
from jinja2 import Environment, FileSystemLoader
import config
import utils


class JSONGenerator:
    
    def __init__(self, project_root: Path, modid: str):
        self.project_root = project_root
        self.modid = modid
        self.assets_path = project_root / "src/main/resources/assets" / modid
        self.data_path = project_root / "src/main/resources/data" / modid
        
        template_dir = Path(__file__).parent.parent / "templates/json"
        self.jinja_env = Environment(loader=FileSystemLoader(str(template_dir)))
    
    def generate_all(self, spec: Dict):
        print("\nGenerating JSON files...")
        
        items = spec.get('items', [])
        blocks = spec.get('blocks', [])
        recipes = spec.get('recipes', [])
        
        self.generate_item_models(items)
        self.generate_block_models(blocks)
        # Ensure items entries for Minecraft 1.21+ (assets/<modid>/items/<id>.json)
        self.generate_items_entries(items, blocks)
        self.generate_blockstates(blocks)
        self.generate_lang_file(items, blocks, spec)
        self.generate_recipes(recipes)
        self.generate_loot_tables(blocks)
        self.generate_sounds_json(spec)
    
    def generate_item_models(self, items: List[Dict]):
        template = self.jinja_env.get_template('item_model.json.j2')
        
        for item in items:
            item_id = item['id']
            texture = item.get('texture', item_id)
            
            content = template.render(
                autogen_comment=config.JSON_AUTOGEN_COMMENT,
                modid=self.modid,
                texture=texture
            )
            
            path = self.assets_path / "models/item" / f"{item_id}.json"
            utils.write_file(path, content)
    
    def generate_block_models(self, blocks: List[Dict]):
        block_template = self.jinja_env.get_template('block_model.json.j2')
        item_template = self.jinja_env.get_template('block_item_model.json.j2')
        
        for block in blocks:
            block_id = block['id']
            textures = block.get('textures', {})
            
            if isinstance(textures, str):
                textures = {'all': textures}
            
            if 'all' not in textures:
                for face in ['north', 'south', 'east', 'west', 'up', 'down']:
                    if face not in textures:
                        textures[face] = textures.get('all', block_id)
            
            content = block_template.render(
                autogen_comment=config.JSON_AUTOGEN_COMMENT,
                modid=self.modid,
                textures=textures
            )
            
            path = self.assets_path / "models/block" / f"{block_id}.json"
            utils.write_file(path, content)
            
            item_content = item_template.render(
                autogen_comment=config.JSON_AUTOGEN_COMMENT,
                modid=self.modid,
                block_id=block_id
            )
            
            path = self.assets_path / "models/item" / f"{block_id}.json"
            utils.write_file(path, item_content)

    def generate_items_entries(self, items: List[Dict], blocks: List[Dict]):
        items_dir = self.assets_path / "items"
        utils.ensure_dir(items_dir)

        for item in items:
            item_id = item['id']
            payload = {
                "_comment": config.JSON_AUTOGEN_COMMENT,
                "model": {
                    "type": "minecraft:model",
                    "model": f"{self.modid}:item/{item_id}"
                }
            }
            path = items_dir / f"{item_id}.json"
            utils.write_file(path, json.dumps(payload, indent=2))

        for block in blocks:
            block_id = block['id']
            payload = {
                "_comment": config.JSON_AUTOGEN_COMMENT,
                "model": {
                    "type": "minecraft:model",
                    "model": f"{self.modid}:block/{block_id}"
                }
            }
            path = items_dir / f"{block_id}.json"
            utils.write_file(path, json.dumps(payload, indent=2))
    
    def generate_blockstates(self, blocks: List[Dict]):
        template = self.jinja_env.get_template('blockstate.json.j2')
        
        for block in blocks:
            block_id = block['id']
            
            content = template.render(
                autogen_comment=config.JSON_AUTOGEN_COMMENT,
                modid=self.modid,
                block_id=block_id
            )
            
            path = self.assets_path / "blockstates" / f"{block_id}.json"
            utils.write_file(path, content)
    
    def generate_lang_file(self, items: List[Dict], blocks: List[Dict], spec: Dict):
        lang_path = self.assets_path / "lang/en_us.json"
        
        existing = {}
        if lang_path.exists():
            try:
                existing = json.loads(lang_path.read_text())
            except:
                pass
        
        new_entries = {}
        
        for item in items:
            key = f"item.{self.modid}.{item['id']}"
            display_name = item.get('display_name', utils.to_title_case(item['id']))
            new_entries[key] = display_name
        
        for block in blocks:
            key = f"block.{self.modid}.{block['id']}"
            display_name = block.get('display_name', utils.to_title_case(block['id']))
            new_entries[key] = display_name
        
        if 'creative_tab' in spec:
            tab = spec['creative_tab']
            key = f"itemGroup.{self.modid}.{tab['id']}"
            new_entries[key] = tab.get('display_name', utils.to_title_case(tab['id']))
        
        merged = {**existing, **new_entries}
        sorted_lang = dict(sorted(merged.items()))
        
        utils.write_file(lang_path, json.dumps(sorted_lang, indent=2))
    
    def generate_recipes(self, recipes: List[Dict]):
        for recipe in recipes:
            recipe_id = recipe['id']
            recipe_type = recipe['type']
            # Basic validation to avoid generating malformed recipe JSON that fails reload
            if recipe_type == 'shapeless':
                ingredients = recipe.get('ingredients', [])
                if not ingredients:
                    print(f"  [WARN] Shapeless recipe '{recipe_id}' has no ingredients; skipping.")
                    continue
            elif recipe_type == 'shaped':
                pattern = recipe.get('pattern', [])
                key = recipe.get('key', {})
                if not pattern or not key:
                    print(f"  [WARN] Shaped recipe '{recipe_id}' missing pattern or key; skipping.")
                    continue
                # Verify all characters in pattern have corresponding key entries
                chars = set(''.join(pattern)) - set(' ')
                missing = [c for c in chars if c not in key]
                if missing:
                    print(f"  [WARN] Shaped recipe '{recipe_id}' missing key entries for: {missing}; skipping.")
                    continue
            
            if recipe_type == 'shaped':
                processed_key = {}
                for char, value in recipe['key'].items():
                    if 'item' in value:
                        item = value['item']
                        if ':' not in item:
                            processed_key[char] = f"{self.modid}:{item}"
                        else:
                            processed_key[char] = item
                    else:
                        processed_key[char] = value
                
                template = self.jinja_env.get_template('shaped_recipe.json.j2')
                content = template.render(
                    autogen_comment=config.JSON_AUTOGEN_COMMENT,
                    modid=self.modid,
                    pattern=recipe['pattern'],
                    key=processed_key,
                    result=recipe['result'],
                    count=recipe.get('count', 1)
                )
            elif recipe_type == 'shapeless':
                processed_ingredients = []
                for ingredient in recipe['ingredients']:
                    if 'item' in ingredient:
                        item = ingredient['item']
                        if ':' not in item:
                            processed_ingredients.append(f"{self.modid}:{item}")
                        else:
                            processed_ingredients.append(item)
                    else:
                        processed_ingredients.append(ingredient)
                
                template = self.jinja_env.get_template('shapeless_recipe.json.j2')
                content = template.render(
                    autogen_comment=config.JSON_AUTOGEN_COMMENT,
                    modid=self.modid,
                    ingredients=processed_ingredients,
                    result=recipe['result'],
                    count=recipe.get('count', 1)
                )
            elif recipe_type == 'smelting':
                template = self.jinja_env.get_template('smelting_recipe.json.j2')
                content = template.render(
                    autogen_comment=config.JSON_AUTOGEN_COMMENT,
                    modid=self.modid,
                    ingredient=recipe['ingredient'],
                    result=recipe['result'],
                    experience=recipe.get('experience', 0.1),
                    cooking_time=recipe.get('cooking_time', 200)
                )
            else:
                print(f"  [WARN] Unknown recipe type: {recipe_type}")
                continue
            
            path = self.data_path / "recipe" / f"{recipe_id}.json"
            utils.write_file(path, content)
    
    def generate_loot_tables(self, blocks: List[Dict]):
        template = self.jinja_env.get_template('loot_table.json.j2')
        
        for block in blocks:
            block_id = block['id']
            drops = block.get('drops')
            
            if drops == 'nothing':
                drops_type = 'nothing'
                drops_item = None
            elif drops is None or drops == 'self':
                drops_type = 'item'
                drops_item = block_id
            else:
                drops_type = 'item'
                drops_item = drops
            
            content = template.render(
                autogen_comment=config.JSON_AUTOGEN_COMMENT,
                modid=self.modid,
                drops_type=drops_type,
                drops_item=drops_item
            )
            
            path = self.data_path / "loot_table/blocks" / f"{block_id}.json"
            utils.write_file(path, content)
    
    def generate_sounds_json(self, spec: Dict):
        sounds = spec.get('sounds', [])
        if not sounds:
            return
        
        template = self.jinja_env.get_template('sounds.json.j2')
        
        sounds_with_files = []
        for sound in sounds:
            sounds_with_files.append({
                'id': sound['id'],
                'file': sound.get('file', sound['id']),
                'category': sound.get('category', 'neutral'),
                'volume': sound.get('volume', 1.0),
                'pitch': sound.get('pitch', 1.0)
            })
        
        sounds_dir = self.assets_path / "sounds"
        source_sounds_dir = self.project_root / "assets" / "sounds"

        if (not sounds_dir.exists() or not any(sounds_dir.iterdir())) and source_sounds_dir.exists():
            utils.ensure_dir(sounds_dir)
            for sound_file in source_sounds_dir.iterdir():
                if sound_file.is_file():
                    shutil.copy2(sound_file, sounds_dir / sound_file.name)

        if not sounds_dir.exists() or not any(sounds_dir.iterdir()):
            print("  [INFO] No sound files found in assets; skipping sounds.json generation.")
            return

        content = template.render(
            autogen_comment=config.JSON_AUTOGEN_COMMENT,
            modid=self.modid,
            sounds=sounds_with_files
        )

        path = self.assets_path / "sounds.json"
        utils.write_file(path, content)

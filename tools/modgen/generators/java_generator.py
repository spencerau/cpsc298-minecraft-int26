from pathlib import Path
from typing import Dict, List, Optional
from jinja2 import Environment, FileSystemLoader
import utils


class JavaGenerator:
    
    def __init__(self, project_root: Path, base_package: str, modid: str, mod_class: str = 'ExampleMod'):
        self.project_root = project_root
        self.base_package = base_package
        self.modid = modid
        self.mod_class = mod_class
        self.java_src = project_root / "src/main/java" / base_package.replace('.', '/')
        self.generated_pkg = f"{base_package}.generated"
        self.custom_pkg = f"{base_package}.custom"
        
        template_dir = project_root / "tools/modgen/templates/java"
        self.env = Environment(loader=FileSystemLoader(str(template_dir)), trim_blocks=True, lstrip_blocks=True)
    
    def generate_all(self, spec: Dict):
        print("\n☕ Generating Java code...")
        
        items = spec.get('items', [])
        blocks = spec.get('blocks', [])
        creative_tab = spec.get('creative_tab')
        
        self.generate_items_class(items)
        self.generate_blocks_class(blocks)
        self.generate_registries_class(items, blocks)
        self.generate_creative_tabs_class(creative_tab, items, blocks)
        self.generate_custom_stubs(items, blocks)
    
    def generate_items_class(self, items: List[Dict]):
        template = self.env.get_template('GeneratedItems.java.j2')
        
        tool_materials = []
        seen_materials = set()
        for item in items:
            if 'tool' in item and 'material' in item['tool']:
                mat = item['tool']['material']
                mat_name = mat.get('name', f"{item['id'].upper()}_MATERIAL")
                if mat_name not in seen_materials:
                    tool_materials.append({
                        'name': mat_name,
                        'incorrect_for_tag': mat.get('incorrect_for_tag', 'INCORRECT_FOR_NETHERITE_TOOL'),
                        'durability': mat.get('durability', 1561),
                        'speed': mat.get('speed', 8.0),
                        'attack_bonus': mat.get('attack_bonus', 3.0),
                        'enchantability': mat.get('enchantability', 15),
                        'repair_tag': mat.get('repair_tag', 'PLANKS')
                    })
                    seen_materials.add(mat_name)
        
        items_data = []
        for item in items:
            item_data = {
                'id': item['id'],
                'var_name': item['id'].upper(),
                'props': self._build_item_properties(item),
                'custom_class': utils.to_camel_case(item['id']) + "Item" if item.get('custom_class') else None,
                'tool': item.get('tool')
            }
            items_data.append(item_data)
        
        code = template.render(
            package=self.generated_pkg,
            base_package=self.base_package,
            modid=self.modid,
            mod_class=self.mod_class,
            items=items_data,
            tool_materials=tool_materials
        )
        
        path = self.java_src / "generated" / "GeneratedItems.java"
        utils.write_file(path, code)
    
    def generate_blocks_class(self, blocks: List[Dict]):
        template = self.env.get_template('GeneratedBlocks.java.j2')
        
        blocks_data = []
        for block in blocks:
            behavior = block.get('behavior', {})
            custom = behavior or block.get('custom_class')
            
            block_data = {
                'id': block['id'],
                'var_name': block['id'].upper(),
                'props': self._build_block_properties(block),
                'custom_class': utils.to_camel_case(block['id']) + "Block" if custom else None
            }
            blocks_data.append(block_data)
        
        code = template.render(
            package=self.generated_pkg,
            base_package=self.base_package,
            modid=self.modid,
            mod_class=self.mod_class,
            blocks=blocks_data
        )
        
        path = self.java_src / "generated" / "GeneratedBlocks.java"
        utils.write_file(path, code)
    
    def generate_registries_class(self, items: List[Dict], blocks: List[Dict]):
        template = self.env.get_template('GeneratedRegistries.java.j2')
        
        code = template.render(
            package=self.generated_pkg
        )
        
        path = self.java_src / "generated" / "GeneratedRegistries.java"
        utils.write_file(path, code)
    
    def generate_creative_tabs_class(self, creative_tab: Optional[Dict], items: List[Dict], blocks: List[Dict]):
        template = self.env.get_template('GeneratedCreativeTabs.java.j2')
        
        if not creative_tab:
            creative_tab = {
                'id': 'generated_tab',
                'display_name': 'Generated Content',
                'icon': items[0]['id'] if items else blocks[0]['id']
            }
        
        item_additions = []
        for item in items:
            item_additions.append(f"GeneratedItems.{item['id'].upper()}")
        
        for block in blocks:
            item_additions.append(f"GeneratedBlocks.{block['id'].upper()}_ITEM")
        
        icon_id = creative_tab.get('icon', items[0]['id'] if items else blocks[0]['id'])
        icon_is_block = any(b['id'] == icon_id for b in blocks)
        icon_ref = f"GeneratedBlocks.{icon_id.upper()}" if icon_is_block else f"GeneratedItems.{icon_id.upper()}"
        
        code = template.render(
            package=self.generated_pkg,
            base_package=self.base_package,
            modid=self.modid,
            mod_class=self.mod_class,
            tab_id=creative_tab['id'],
            icon_item=icon_ref,
            item_additions=item_additions
        )
        
        path = self.java_src / "generated" / "GeneratedCreativeTabs.java"
        utils.write_file(path, code)
    
    def generate_custom_stubs(self, items: List[Dict], blocks: List[Dict]):
        for item in items:
            if not item.get('custom_class'):
                continue
            
            template = self.env.get_template('CustomItem.java.j2')
            
            item_id = item['id']
            class_name = utils.to_camel_case(item_id) + "Item"
            
            behavior = item.get('behavior')
            behavior_methods = []
            if behavior:
                if 'on_finish_eating' in behavior:
                    effects = behavior['on_finish_eating'].get('effects', [])
                    if effects:
                        behavior_methods.append({
                            'name': 'finishUsingItem',
                            'effects': effects
                        })
                
                if 'on_use' in behavior:
                    on_use = behavior['on_use']
                    if on_use.get('lightning'):
                        behavior_methods.append({
                            'name': 'use',
                            'action': 'summon_lightning'
                        })
            
            code = template.render(
                package=self.custom_pkg,
                class_name=class_name,
                item_id=item_id,
                behavior_methods=behavior_methods
            )
            
            path = self.java_src / "custom" / f"{class_name}.java"
            utils.write_file(path, code, overwrite=False)
        
        for block in blocks:
            behavior = block.get('behavior')
            if not behavior and not block.get('custom_class'):
                continue
            
            template = self.env.get_template('CustomBlock.java.j2')
            
            block_id = block['id']
            class_name = utils.to_camel_case(block_id) + "Block"
            
            behavior_methods = []
            if behavior:
                if 'bounce_strength' in behavior:
                    behavior_methods.append({
                        'name': 'stepOn',
                        'bounce_strength': behavior['bounce_strength']
                    })
                    behavior_methods.append({
                        'name': 'fallOn',
                        'fall_damage_multiplier': behavior.get('fall_damage_multiplier', 0.0)
                    })
                
                if 'explosion' in behavior:
                    explosion = behavior['explosion']
                    behavior_methods.append({
                        'name': 'playerDestroy',
                        'explosion': {
                            'power': explosion.get('power', 2.0),
                            'fire': explosion.get('fire', False),
                            'destroy_blocks': explosion.get('destroy_blocks', True)
                        }
                    })
            
            code = template.render(
                package=self.custom_pkg,
                class_name=class_name,
                block_id=block_id,
                behavior_methods=behavior_methods
            )
            
            path = self.java_src / "custom" / f"{class_name}.java"
            utils.write_file(path, code, overwrite=False)
    
    def _build_item_properties(self, item: Dict) -> str:
        if 'tool' in item:
            tool = item['tool']
            if 'material' in tool:
                mat_name = tool['material'].get('name', f"{item['id'].upper()}_MATERIAL")
                durability = tool['material'].get('durability', 1561)
                return f"new Item.Properties().durability({durability})"
        
        props = []
        if 'stack_size' in item:
            props.append(f"stacksTo({item['stack_size']})")
        if 'rarity' in item:
            props.append(f"rarity(Rarity.{item['rarity'].upper()})")
        if 'food' in item:
            food = item['food']
            nutrition = food.get('nutrition', 1)
            saturation = food.get('saturation', 0.1)
            props.append(f"food(new FoodProperties.Builder().nutrition({nutrition}).saturationModifier({saturation}f).build())")
        
        return "new Item.Properties()" + ('.' + '.'.join(props) if props else '')

    
    def _build_block_properties(self, block: Dict) -> str:
        props = []
        if 'hardness' in block:
            resistance = block.get('resistance', block['hardness'])
            props.append(f"strength({block['hardness']}f, {resistance}f)")
        if 'light_level' in block:
            props.append(f"lightLevel(state -> {block['light_level']})")
        if 'sound_type' in block:
            props.append(f"sound(SoundType.{block['sound_type'].upper()})")
        
        return "BlockBehaviour.Properties.of()" + ('.' + '.'.join(props) if props else '')

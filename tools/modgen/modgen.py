import sys
from pathlib import Path
import yaml

# Add the modgen directory to the path so we can import from generators
sys.path.insert(0, str(Path(__file__).parent))

from generators import AssetManager, JSONGenerator, JavaGenerator


# ============================================================================
# Main Generator
# ============================================================================

class ModGenerator:
    
    def __init__(self, project_root: Path):
        self.project_root = project_root
        self.spec_path = project_root / "tools/modgen/content.yaml"
        
    def load_spec(self) -> dict:
        print(f"Loading spec from {self.spec_path}...")
        
        if not self.spec_path.exists():
            raise FileNotFoundError(f"Spec file not found: {self.spec_path}")
        
        with open(self.spec_path, 'r') as f:
            spec = yaml.safe_load(f)
        
        required = ['modid', 'base_package']
        for field in required:
            if field not in spec:
                raise ValueError(f"Missing required field in spec: {field}")
        
        return spec
    
    def generate(self):
        print("=" * 70)
        print("NeoForge Mod Content Generator")
        print("=" * 70)
        
        spec = self.load_spec()
        modid = spec['modid']
        base_package = spec['base_package']
        mod_class = spec.get('mod_class', 'ExampleMod')
        
        print(f"Mod ID: {modid}")
        print(f"Base Package: {base_package}")
        
        asset_mgr = AssetManager(self.project_root, modid)
        json_gen = JSONGenerator(self.project_root, modid)
        java_gen = JavaGenerator(self.project_root, base_package, modid, mod_class)
        
        items = spec.get('items', [])
        blocks = spec.get('blocks', [])
        
        asset_mgr.copy_textures(items, blocks)
        asset_mgr.copy_sounds(items, blocks)
        
        json_gen.generate_all(spec)
        java_gen.generate_all(spec)
        
        print("\n" + "=" * 70)
        print("Generation complete!")
        print("=" * 70)
        print("\nNext steps:")
        print("  1. Review generated files in src/main/java/.../generated/")
        print("  2. Add GeneratedRegistries.init() to your main mod class")
        print("  3. Run ./gradlew build to compile")
        print("  4. Run ./gradlew runClient to test")


# ============================================================================
# Entry Point
# ============================================================================

def main():
    script_path = Path(__file__).resolve()
    project_root = script_path.parent.parent.parent
    
    try:
        generator = ModGenerator(project_root)
        generator.generate()
        return 0
    except Exception as e:
        print(f"\nError: {e}", file=sys.stderr)
        import traceback
        traceback.print_exc()
        return 1


if __name__ == '__main__':
    sys.exit(main())

import sys
import shutil
from pathlib import Path
import yaml


def clean_generated_content(project_root: Path):
    print("Cleaning generated mod content...")
    print("=" * 70)

    spec_path = project_root / "tools/modgen/content.yaml"
    
    if not spec_path.exists():
        print(f"Warning: {spec_path} not found, skipping clean")
        return
    
    with open(spec_path, 'r') as f:
        spec = yaml.safe_load(f)
        modid = spec['modid']
        base_pkg = spec['base_package']

    base_pkg_path = Path(base_pkg.replace('.', '/'))

    base_package_main_java = project_root / "src/main/java" / base_pkg_path
    resources_path = project_root / "src/main/resources"
    generated_resources_path = project_root / "src/generated/resources"

    paths_to_remove = [
        base_package_main_java / "generated",
        base_package_main_java / "custom",
        resources_path / f"assets/{modid}",
        resources_path / f"data/{modid}",
        generated_resources_path,
    ]

    removed_count = 0
    for path in paths_to_remove:
        if path.exists():
            if path.is_dir():
                shutil.rmtree(path)
                print(f"[DELETE] {path.relative_to(project_root)}/")
            else:
                path.unlink()
                print(f"[DELETE] {path.relative_to(project_root)}")
            removed_count += 1

    print("=" * 70)
    print(f"Removed {removed_count} paths")
    print("Clean complete!")


if __name__ == '__main__':
    project_root = Path(__file__).parent.parent.parent
    clean_generated_content(project_root)

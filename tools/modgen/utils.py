from pathlib import Path


def ensure_dir(path: Path):
    path.mkdir(parents=True, exist_ok=True)


def write_file(path: Path, content: str, overwrite: bool = True):
    if not overwrite and path.exists():
        print(f"  [SKIP] {path} (already exists)")
        return
    ensure_dir(path.parent)
    path.write_text(content, encoding='utf-8')
    print(f"  [WRITE] {path}")


def to_snake_case(name: str) -> str:
    return name.lower().replace(' ', '_')


def to_camel_case(name: str) -> str:
    return ''.join(word.capitalize() for word in name.replace('_', ' ').split())


def to_title_case(name: str) -> str:
    return name.replace('_', ' ').title()

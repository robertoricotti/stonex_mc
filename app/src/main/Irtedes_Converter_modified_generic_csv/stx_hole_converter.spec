# -*- mode: python ; coding: utf-8 -*-


a = Analysis(
    ['stx_hole_converter.py'],
    pathex=[],
    binaries=[],
    datas=[('templates\\Jet_Template.xlsx', 'templates'), ('templates\\Rock_Template.xlsx', 'templates'), ('templates\\Solar_Template.xlsx', 'templates')],
    hiddenimports=[],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    noarchive=False,
    optimize=0,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name='stx_hole_converter',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon=['stx_icon.ico'],
)

"""Transform real released Goety bytecode with the game's Mixin 0.8.5 (no Minecraft startup).

Usage: python tools/test_final_staff_mixins.py --minecraft-root D:/MC/.minecraft
       --addon build/releases/starfantasy_goety-0.5.45-clean.jar --goety path/to/goety.jar
Run once per supported Goety version, after tools/build.ps1.
"""
import argparse
import json
import os
from pathlib import Path
import subprocess
import time

p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--minecraft-root', required=True, type=Path)
p.add_argument('--addon', required=True, type=Path)
p.add_argument('--goety', required=True, type=Path)
p.add_argument('--health', action='store_true', help='Verify the boss health guards instead of staff priority hooks')
p.add_argument('--apostle-servant', action='store_true', help='Verify native Apostle servant isolation and existing Apostle hooks')
args = p.parse_args()
root = Path(__file__).resolve().parents[1]
build = root / 'build' / f'mixin-validation-{time.time_ns()}'
build.mkdir(parents=True)
services = build / 'META-INF/services'
services.mkdir(parents=True)
(services / 'org.spongepowered.asm.service.IMixinService').write_text('validation.HeadlessMixinService\n')
(services / 'org.spongepowered.asm.service.IGlobalPropertyService').write_text('validation.HeadlessProperties\n')
mixins = ['FinalStaffZombiePriorityMixin', 'FinalStaffSkeletonPriorityMixin', 'FinalStaffSlimePriorityMixin',
          'FinalStaffHuntingPriorityMixin', 'FinalStaffMaulingPriorityMixin', 'SoulBoltSpellMixin']
(build / 'priority-validation.mixins.json').write_text(json.dumps({
    'required': True, 'minVersion': '0.8', 'compatibilityLevel': 'JAVA_17',
    'package': 'com.starfantasy.goety.mixin', 'mixins': mixins,
    'plugin': 'com.starfantasy.goety.compat.FinalStaffPriorityMixinPlugin',
    'injectors': {'defaultRequire': 1}}))
(build / 'health-validation.mixins.json').write_text(json.dumps({
    'required': True, 'minVersion': '0.8', 'compatibilityLevel': 'JAVA_17',
    'package': 'com.starfantasy.library.mixin',
    'mixins': ['LivingHealthDataAccessor', 'CombatHealthDataMixin', 'LivingEntityCombatMixin'],
    'refmap': 'star_fantasy_library.refmap.json',
    'injectors': {'defaultRequire': 1}}))
(build / 'apostle-servant-validation.mixins.json').write_text(json.dumps({
    'required': True, 'minVersion': '0.8', 'compatibilityLevel': 'JAVA_17',
    'package': 'com.starfantasy.goety.mixin',
    'mixins': ['ApostleAppearanceMixin', 'ApostleMixin'],
    'injectors': {'defaultRequire': 1}}))
latest_args = max(root.glob('build/reproducible-*/main-partial.args'), key=lambda x: x.stat().st_mtime)
lines = latest_args.read_text('utf-8-sig').splitlines()
old_cp = lines[lines.index('"-classpath"') + 1].strip('"')
libs = args.minecraft_root / 'libraries'
preferred = [libs / 'org/spongepowered/mixin/0.8.5/mixin-0.8.5.jar']
preferred += [libs / 'cpw/mods/modlauncher/10.0.9/modlauncher-10.0.9.jar']
preferred += [libs / f'org/ow2/asm/{name}/9.5/{name}-9.5.jar'
              for name in ['asm', 'asm-tree', 'asm-analysis', 'asm-util', 'asm-commons']]
cp = os.pathsep.join([str(build), *(str(x.resolve()) for x in preferred),
                      str(args.addon.resolve()), str(args.goety.resolve()), old_cp])
def run_argfile(name, executable, arguments):
    file = build / name
    file.write_text('\n'.join('"' + str(x).replace('\\', '/').replace('"', '\\"') + '"' for x in arguments), encoding='utf-8')
    result = subprocess.run([executable, '@' + str(file)], cwd=build, capture_output=True, text=True, encoding='utf-8', errors='replace')
    (build / (name + '.log')).write_text(result.stdout + result.stderr, encoding='utf-8')
    if result.returncode:
        print((result.stdout + result.stderr)[-14000:])
        raise SystemExit(f'FAIL {name}; full log: {build / (name + ".log")}')
    print(result.stdout.strip())
run_argfile('compile.args', 'javac', ['-proc:none', '--release', '17', '-encoding', 'UTF-8', '-cp', cp, '-d', build,
                                    *sorted((root / 'tools/mixin-test').glob('*.java'))])
if args.apostle_servant:
    run_argfile('apostle-servant.args', 'java', ['-cp', cp, 'validation.ApostleServantMixinRegression'])
elif args.health:
    run_argfile('health.args', 'java', ['-cp', cp, 'validation.HealthMixinRegression'])
else:
    for scenario in ['normal', 'shift-locals', 'missing-hook', 'renamed-local']:
        run_argfile(scenario + '.args', 'java', ['-cp', cp, 'validation.PriorityMixinRegression', scenario])
print(f'PASS Goety: {args.goety.name}; logs: {build}')

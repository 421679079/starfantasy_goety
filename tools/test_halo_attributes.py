"""Run actual halo modifier and library damage-reduction methods with attribute/tag fixtures."""
from pathlib import Path
import json
import re
import subprocess
import time
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
LIB = ROOT.parent / 'star_fantasy_library'
OUT = ROOT / 'build' / f'halo-attributes-{time.time_ns()}'
OUT.mkdir(parents=True)


def method(source, marker):
    start = source.index(marker)
    end = source.index('{', start) + 1
    depth = 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


halo = (ROOT / 'src/main/java/com/starfantasy/goety/item/HaloCurioItem.java').read_text('utf-8')
combat = (LIB / 'src/main/java/com/starfantasy/library/mixin/LivingEntityCombatMixin.java').read_text('utf-8')
halo_methods = '\n'.join(method(halo, s) for s in ['public Multimap<Attribute, AttributeModifier> getAttributeModifiers(', 'private static AttributeModifier modifier('])
combat_methods = '\n'.join(method(combat, s) for s in ['private void starFantasyLibrary$applyDamageReductions(', 'private static double starFantasyLibrary$ratio('])
attributes = re.findall(r'public static final RegistryObject<Attribute> (\w+) = reduction',
                       (LIB / 'src/main/java/com/starfantasy/library/combat/StarFantasyCombatAttributes.java').read_text('utf-8'))
tags = re.findall(r'public static final TagKey<DamageType> (\w+) = create',
                 (LIB / 'src/main/java/com/starfantasy/library/combat/StarFantasyDamageTypeTags.java').read_text('utf-8'))
fixture = r'''
import java.util.*;
public class HaloAttributeRegression {
 static int checks;
 static void eq(double expected,double actual){checks++;if(Math.abs(expected-actual)>.00001)throw new AssertionError(expected+" != "+actual);}
 static class Attribute {}
 record Registry<T>(T get) {}
 record AttributeModifier(UUID id,String name,double amount,Operation operation){enum Operation{ADDITION,MULTIPLY_BASE,MULTIPLY_TOTAL}}
 static class Multimap<K,V> extends HashMap<K,V>{}
 static class ImmutableMultimap {
  static <K,V> Builder<K,V> builder(){return new Builder<>();}
  static class Builder<K,V>{Multimap<K,V> values=new Multimap<>();void put(K k,V v){values.put(k,v);}Multimap<K,V> build(){return values;}}
 }
 static class SlotContext {}static class ItemStack {}
 static class StarFantasyCombatAttributes { ATTRIBUTES }
 static class StarFantasyDamageTypeTags { TAGS }
 static class SpellAttributeRegistry {
  static final Registry<Attribute> SPELL_POWER=new Registry<>(new Attribute()),COOLDOWN_REDUCTION=new Registry<>(new Attribute());
  static Registry<Attribute> spellPowerFor(Object s){return SPELL_POWER;}static Registry<Attribute> cooldownFor(Object s){return COOLDOWN_REDUCTION;}
 }
 static class Halo {
  static final double SPELL_POWER=1,COOLDOWN_REDUCTION=.1,RESISTANCE=.1;
  Object school;boolean hades;
  HALO_METHODS
 }
 static class HaloItemRegistry {
  HALO_FIELDS
 }
 static class AttributeInstance {double value;AttributeInstance(double v){value=v;}double getValue(){return value;}}
 static class LivingEntity {
  Map<Attribute,AttributeInstance> values=new HashMap<>();AttributeInstance getAttribute(Attribute a){return values.get(a);}
 }
 static class DamageSource {
  Set<String> tags;DamageSource(String...t){tags=Set.of(t);}boolean is(String t){return tags.contains(t);}
 }
 static class DamageTypeTags {static final String BYPASSES_RESISTANCE="bypass";}
 static class Mth {static double clamp(double v,double a,double b){return Math.max(a,Math.min(b,v));}}
 static class CallbackInfoReturnable<T>{T value;CallbackInfoReturnable(T v){value=v;}T getReturnValue(){return value;}void setReturnValue(T v){value=v;}}
 static class Applied extends LivingEntity {
  COMBAT_METHODS
  float damage(String...tags){var c=new CallbackInfoReturnable<Float>(100F);starFantasyLibrary$applyDamageReductions(new DamageSource(tags),100F,c);return c.value;}
 }
 static final UUID SLOT=UUID.randomUUID();
 static void checkHalo(Halo halo,Attribute attribute,double fraction,String tag){
  var modifiers=halo.getAttributeModifiers(new SlotContext(),SLOT,new ItemStack());
  AttributeModifier modifier=modifiers.get(attribute);eq(fraction,modifier.amount());
  if(modifier.operation()!=AttributeModifier.Operation.MULTIPLY_TOTAL)throw new AssertionError("Expected percent modifier");
  Applied player=new Applied();player.values.put(attribute,new AttributeInstance(1*(1+modifier.amount())));
  eq(100*(1-fraction),player.damage(tag));eq(100,player.damage("unrelated"));
 }
 public static void main(String[] args){
  CHECK_HALOS
  var penetration=HaloItemRegistry.HALO_OF_THE_ATROCIOUS.get().getAttributeModifiers(new SlotContext(),SLOT,new ItemStack()).get(StarFantasyCombatAttributes.ARMOR_PENETRATION.get());
  eq(.2,penetration.amount());if(penetration.operation()!=AttributeModifier.Operation.MULTIPLY_TOTAL)throw new AssertionError();
  Halo caster=new Halo();caster.school=new Object();var spell=caster.getAttributeModifiers(new SlotContext(),SLOT,new ItemStack()).get(SpellAttributeRegistry.SPELL_POWER.get());
  eq(1,spell.amount());if(spell.operation()!=AttributeModifier.Operation.ADDITION)throw new AssertionError("Spell power must remain flat");
  Halo hades=new Halo();hades.hades=true;checkHalo(hades,StarFantasyCombatAttributes.RESISTANCE.get(),.1,"ordinary");
  Applied p=new Applied();p.values.put(StarFantasyCombatAttributes.POISON_RESISTANCE.get(),new AttributeInstance(1.25));
  p.values.put(StarFantasyCombatAttributes.MAGIC_RESISTANCE.get(),new AttributeInstance(1.5));
  eq(37.5,p.damage(StarFantasyDamageTypeTags.POISON_DAMAGE,StarFantasyDamageTypeTags.MAGIC_DAMAGE));
  p.values.put(StarFantasyCombatAttributes.POISON_RESISTANCE.get(),new AttributeInstance(2));eq(0,p.damage(StarFantasyDamageTypeTags.POISON_DAMAGE));
  p.values.put(StarFantasyCombatAttributes.POISON_RESISTANCE.get(),new AttributeInstance(.5));eq(100,p.damage(StarFantasyDamageTypeTags.POISON_DAMAGE));
  System.out.println("PASS "+checks+" halo checks: percent modifiers, unchanged spell power, all categories, absent attributes and composite damage");
 }
}
'''
names = ['PYRE_LORD', 'GLORIOUS', 'ATROCIOUS', 'CRUEL', 'DEFILER', 'TERRIBLE', 'DARK', 'GREAT_SHADOW']
cases = [('PYRE_LORD', 'FIRE', .25), ('GLORIOUS', 'PHYSICAL', .1), ('CRUEL', 'FROST', .25),
         ('DEFILER', 'POISON', .25), ('TERRIBLE', 'LIGHTNING', .25), ('DARK', 'VOID', .1), ('GREAT_SHADOW', 'PROJECTILE', .15)]
fixture = fixture.replace('ATTRIBUTES', '\n'.join('static final Registry<Attribute> '+a+'=new Registry<>(new Attribute());' for a in attributes))
fixture = fixture.replace('TAGS', '\n'.join('static final String '+t+'="'+t+'";' for t in tags))
fixture = fixture.replace('HALO_FIELDS', '\n'.join('static final Registry<Halo> HALO_OF_THE_'+n+'=new Registry<>(new Halo());' for n in names))
fixture = fixture.replace('CHECK_HALOS', '\n'.join('checkHalo(HaloItemRegistry.HALO_OF_THE_'+n+'.get(),StarFantasyCombatAttributes.'+a+'_RESISTANCE.get(),'+str(v)+',StarFantasyDamageTypeTags.'+a+'_DAMAGE);' for n,a,v in cases))
fixture = fixture.replace('HALO_METHODS', halo_methods).replace('COMBAT_METHODS', combat_methods)
# RESISTANCE also reduces an unrelated ordinary damage type; omit this check for its generic category.
fixture = fixture.replace('eq(100,player.damage("unrelated"));', 'if(attribute!=StarFantasyCombatAttributes.RESISTANCE.get())eq(100,player.damage("unrelated"));')
java = OUT / 'HaloAttributeRegression.java'
java.write_text(fixture, encoding='utf-8')
subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(OUT), str(java)], check=True)
subprocess.run(['java', '-ea', '-cp', str(OUT), 'HaloAttributeRegression'], check=True)

# Verify the merged tags classify the exact original damage types without making all magic poisonous.
with ZipFile(ROOT.parent/'private-deps/goety/goety-2.5.56.5.jar') as goety:
    def values(namespace, name):
        path = f'data/{namespace}/tags/damage_type/{name}.json'
        found = []
        for base in [LIB, ROOT]:
            p = base/'src/main/resources'/path
            if p.exists():
                found += json.loads(p.read_text('utf-8'))['values']
        if path in goety.namelist():
            found += json.loads(goety.read(path))['values']
        result = set()
        for entry in found:
            value = entry if isinstance(entry,str) else entry['id']
            if value.startswith('#goety:'):
                result |= values('goety', value.split(':',1)[1])
            else:
                result.add(value)
        return result
    assert values('star_fantasy','poison_damage') == {'goety:acid','goety:venom'}
    assert values('star_fantasy','void_damage') == {'minecraft:out_of_world','goety:voided'}
    assert 'goety:frost_breath' in values('star_fantasy','frost_damage')
    assert 'goety:shock' in values('star_fantasy','lightning_damage')
    assert '#minecraft:is_projectile' in values('star_fantasy','projectile_damage')
    assert 'minecraft:starve' not in set().union(*(values('star_fantasy',k+'_damage') for k in ['frost','poison','lightning','void','projectile']))
print('PASS merged vanilla/Goety damage tags; starvation remains outside the new reductions.')

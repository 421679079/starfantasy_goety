"""Source-level depth opt-in regression checks; no Java build or game launch."""
from pathlib import Path
import re
import unittest


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/starfantasy/goety'
LIB = ROOT.parent / 'star_fantasy_library/src/main/java/com/starfantasy/library/vfx'


class StarArrowDepthTest(unittest.TestCase):
    def test_flying_head_and_trail_opt_in(self):
        for entity in ('ApollyonStarArrowEntity', 'ApostleMeteorEntity'):
            with self.subTest(entity=entity):
                source = (JAVA / f'entity/{entity}.java').read_text(encoding='utf-8')
                self.assertRegex(source, r'@Override\s+public boolean '
                                 r'starFantasyStarArrowDepthTested\(\)\s*\{\s*return true;\s*\}')

    def test_lingering_trails_keep_scene_depth(self):
        for renderer in ('ApollyonStarArrowRenderer', 'ApostleMeteorRenderer'):
            with self.subTest(renderer=renderer):
                source = (JAVA / f'client/{renderer}.java').read_text(encoding='utf-8')
                self.assertIn('StarFantasyStarArrowVisualRenderer.renderAtEntityOrigin(', source)
                self.assertIn('StarFantasyStarArrowVisualRenderer.renderWorldTrailDepthTested(', source)
                self.assertNotIn('StarFantasyStarArrowVisualRenderer.renderWorldTrail(', source)

    def test_library_routes_both_layers_to_depth_tested_types(self):
        source = (LIB / 'client/StarFantasyStarArrowVisualRenderer.java').read_text(encoding='utf-8')
        for render_type in ('depthLightParticle(TRAIL_TEXTURE)', 'depthParticle(STAR_TEXTURE)'):
            with self.subTest(render_type=render_type):
                self.assertRegex(source, r'visual\.starFantasyStarArrowDepthTested\(\)\s*'
                                 r'\? StarFantasyVfxRenderTypes\.' + re.escape(render_type))


if __name__ == '__main__':
    unittest.main()

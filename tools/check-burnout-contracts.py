#!/usr/bin/env python3
"""Static regression checks. These do NOT replace NeoForge compilation/GameTests."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/com/github/wolfiewaffle/hardcore_torches'

def code(path):
    return (JAVA / path).read_text()

class Contracts(unittest.TestCase):
    def test_lantern_has_no_periodic_ticker(self):
        body = code('block/AbstractLanternBlock.java').split('BlockEntityTicker<T> getTicker', 1)[1].split('\n    }', 1)[0]
        self.assertIn('return null;', body)
        self.assertNotIn('.tick()', body)

    def test_only_server_smoldering_keeps_torch_ticker(self):
        body = code('block/AbstractHardcoreTorchBlock.java').split('BlockEntityTicker<T> getTicker', 1)[1].split('\n    }', 1)[0]
        self.assertIn('world.isClientSide', body)
        self.assertIn('ETorchState.SMOLDERING', body)
        self.assertNotIn('ETorchState.LIT', body)

    def test_campfire_has_no_area_scan_or_stack_kill(self):
        body = code('blockentity/HardcoreCampfireBlockEntity.java')
        for forbidden in ('getEntitiesOfClass', 'getItemsAtAndAbove', 'Shapes.or', 'fuel--', '.kill()'):
            self.assertNotIn(forbidden, body)
        self.assertIn('remaining.shrink(1)', body)
        self.assertIn('CampfireBlockEntity.cookTick', body)
        self.assertIn('CampfireBlockEntity.cooldownTick', body)

    def test_contact_hook_keeps_vanilla_behavior(self):
        body = code('block/HardcoreCampfire.java')
        self.assertIn('protected void entityInside(', body)
        self.assertIn('tryAddFuel(item)', body)
        self.assertIn('super.entityInside(state, world, pos, entity)', body)

    def test_scheduler_has_budget_and_unload_cleanup(self):
        body = code('burnout/BurnoutScheduler.java')
        self.assertIn('MAX_EVENTS_PER_TICK', body)
        self.assertIn('LevelEvent.Unload', body)
        self.assertIn('ServerStoppedEvent', body)
        self.assertIn('getChunkNow(', body)
        self.assertNotIn('.getChunk(', body)
        self.assertIn('runsNormally()', body)

    def test_target_metadata(self):
        text = (ROOT / 'gradle.properties').read_text()
        self.assertIn('minecraft_version=1.21.1\n', text)
        self.assertIn('minecraft_version_range=[1.21.1]\n', text)
        self.assertIn('neo_version=21.1.219\n', text)

if __name__ == '__main__':
    unittest.main(verbosity=2)

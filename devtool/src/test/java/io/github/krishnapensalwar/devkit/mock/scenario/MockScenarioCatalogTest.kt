package io.github.krishnapensalwar.devkit.mock.scenario

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockScenarioCatalogTest {

    @Test
    fun builtInsCoverRequiredGroups() {
        val groups = MockScenarioCatalog.builtIns.map { it.group }.toSet()
        assertTrue(groups.contains(MockScenarioGroup.SUCCESS))
        assertTrue(groups.contains(MockScenarioGroup.CLIENT_ERROR))
        assertTrue(groups.contains(MockScenarioGroup.SERVER_ERROR))
        assertTrue(groups.contains(MockScenarioGroup.NETWORK))
        assertTrue(groups.contains(MockScenarioGroup.PERFORMANCE))
    }

    @Test
    fun clientErrorCodesMatchSpec() {
        val codes = MockScenarioCatalog.builtIns
            .filter { it.group == MockScenarioGroup.CLIENT_ERROR }
            .mapNotNull { it.statusCode }
            .toSet()
        assertEquals(setOf(400, 401, 403, 404, 409, 422, 429), codes)
    }

    @Test
    fun serverErrorCodesMatchSpec() {
        val codes = MockScenarioCatalog.builtIns
            .filter { it.group == MockScenarioGroup.SERVER_ERROR }
            .mapNotNull { it.statusCode }
            .toSet()
        assertEquals(setOf(500, 502, 503, 504), codes)
    }

    @Test
    fun networkScenariosHaveNoHttpStatus() {
        MockScenarioCatalog.builtIns
            .filter { it.group == MockScenarioGroup.NETWORK }
            .forEach { assertEquals(null, it.statusCode) }
    }

    @Test
    fun builtInKeysAreNotCustom() {
        MockScenarioCatalog.builtIns.forEach { scenario ->
            assertFalse(scenario.key.startsWith("custom:"))
            assertEquals(scenario.type.name, scenario.key)
        }
    }

    @Test
    fun slowDelayPresetsIncludeRequestedValues() {
        assertEquals(listOf(500L, 1_000L, 2_000L, 3_000L, 5_000L), MockScenarioCatalog.slowDelayPresetsMs)
    }

    @Test
    fun parseCustomId() {
        assertEquals(42L, parseCustomId("custom:42"))
        assertEquals(null, parseCustomId("HTTP_500"))
        assertEquals("custom:9", customKey(9))
    }
}

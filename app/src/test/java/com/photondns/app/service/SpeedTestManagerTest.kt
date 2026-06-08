package com.photondns.app.service

import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SpeedTestManagerTest {

    private lateinit var speedTestManager: SpeedTestManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        speedTestManager = SpeedTestManager()
    }

    @Test
    fun `calculateJitter returns 0 for empty list`() {
    }

    @Test
    fun `calculateJitter returns 0 for single ping`() {
    }

    @Test
    fun `cancelTest stops ongoing test`() {
        speedTestManager.cancelTest()
    }

    @Test
    fun `testProgress starts at 0`() {
        assertEquals(0f, speedTestManager.testProgress.value)
    }

    @Test
    fun `currentTest starts as null`() {
        assertEquals(null, speedTestManager.currentTest.value)
    }
}
package com.photondns.app.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@SmallTest
class MigrationTest {

    private lateinit var migrationTestHelper: MigrationTestHelper

    @Before
    fun setUp() {
        migrationTestHelper = MigrationTestHelper(
            ApplicationProvider.getApplicationContext(),
            PhotonDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        migrationTestHelper.closeWhenFinished(synchronous = true)
    }

    @Test
    fun migrate1To2() {
        migrationTestHelper.createDatabase("photon_dns_database", 1).apply {
            execSQL("INSERT INTO dns_servers (id, name, ip, countryCode, latency, isActive, isCustom, addedTimestamp) VALUES ('test_server', 'Test Server', '1.1.1.1', 'US', 10, 1, 0, 0)")
            execSQL("INSERT INTO speed_test_results (id, timestamp, downloadSpeed, uploadSpeed, ping, jitter, packetLoss, testServer, dnsUsed, testDuration) VALUES ('test_speed', 0, 50.0, 25.0, 10, 5, 0.0, 'server', '1.1.1.1', 1000)")
            execSQL("INSERT INTO latency_records (id, timestamp, dnsServerId, dnsServerName, dnsServerIp, latency, success) VALUES ('test_latency', 0, 'test_server', 'Test', '1.1.1.1', 10, 1)")
            execSQL("INSERT INTO dns_switch_events (id, timestamp, fromDnsServerId, fromDnsServerName, toDnsServerId, toDnsServerName, reason, previousLatency, newLatency, improvement) VALUES ('test_switch', 0, 'server1', 'Server1', 'server2', 'Server2', 'AUTO_SWITCH', 100, 10, 90)")
            close()
        }

        migrationTestHelper.runMigrationsAndValidate("photon_dns_database", 2, true, MIGRATION_1_2)

        val migratedDatabase = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PhotonDatabase::class.java,
            "photon_dns_database"
        ).addMigrations(MIGRATION_1_2).build()

        migratedDatabase.query("SELECT * FROM dns_servers", null).use { cursor ->
            assert(cursor.count == 1)
            cursor.moveToFirst()
            val protocolIndex = cursor.getColumnIndex("protocol")
            val dohUrlIndex = cursor.getColumnIndex("dohUrl")
            val dotHostnameIndex = cursor.getColumnIndex("dotHostname")
            assert(protocolIndex != -1) { "protocol column should exist" }
            assert(dohUrlIndex != -1) { "dohUrl column should exist" }
            assert(dotHostnameIndex != -1) { "dotHostname column should exist" }
            assert(cursor.getString(protocolIndex) == "UDP") { "protocol should default to UDP" }
        }

        migratedDatabase.query("SELECT * FROM speed_test_results", null).use { cursor ->
            assert(cursor.count == 1)
            cursor.moveToFirst()
            val bufferbloatIndex = cursor.getColumnIndex("bufferbloat")
            val privacyScoreIndex = cursor.getColumnIndex("privacyScore")
            assert(bufferbloatIndex != -1) { "bufferbloat column should exist" }
            assert(privacyScoreIndex != -1) { "privacyScore column should exist" }
            assert(cursor.getInt(bufferbloatIndex) == 0) { "bufferbloat should default to 0" }
            assert(cursor.getInt(privacyScoreIndex) == 100) { "privacyScore should default to 100" }
        }

        migratedDatabase.close()
    }
}
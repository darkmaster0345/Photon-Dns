package com.photondns.app.data.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.photondns.app.data.models.DNSServer
import com.photondns.app.data.models.DNSProtocol
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private lateinit var migrationTestHelper: MigrationTestHelper

    @Before
    fun setup() {
        migrationTestHelper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            PhotonDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        migrationTestHelper.closeWhenFinished()
    }

    @Test
    fun migrate1To2() {
        // Create database with version 1 schema
        var database = migrationTestHelper.createDatabase("photon_dns_database", 1)
        
        // Insert data into version 1 tables (without new columns)
        database.execSQL(
            "INSERT INTO dns_servers (id, name, ip, countryCode, latency, isActive, isCustom, addedTimestamp) " +
            "VALUES ('test_server', 'Test Server', '1.1.1.1', 'US', 50, 1, 0, 1234567890)"
        )
        database.execSQL(
            "INSERT INTO speed_test_results (id, timestamp, downloadSpeed, uploadSpeed, ping, jitter, packetLoss, testServer, dnsUsed, testDuration) " +
            "VALUES ('test_result', 1234567890, 100.0, 50.0, 25, 5, 0.0, 'test_server', 'test_server', 5000)"
        )
        
        database.close()

        // Perform migration to version 2
        database = migrationTestHelper.runMigrationsAndValidate(
            "photon_dns_database",
            2,
            true,
            MIGRATION_1_2
        )

        // Verify data migration for dns_servers
        val cursor = database.query("SELECT id, name, ip, countryCode, protocol, dohUrl, dotHostname FROM dns_servers")
        cursor.moveToFirst()
        
        assertEquals("test_server", cursor.getString(0))
        assertEquals("Test Server", cursor.getString(1))
        assertEquals("1.1.1.1", cursor.getString(2))
        assertEquals("US", cursor.getString(3))
        
        // New columns should have default values
        assertEquals("UDP", cursor.getString(4)) // protocol default
        assertEquals(null, cursor.getString(5)) // dohUrl default NULL
        assertEquals(null, cursor.getString(6)) // dotHostname default NULL
        
        cursor.close()

        // Verify data migration for speed_test_results
        val speedCursor = database.query("SELECT id, ping, bufferbloat, privacyScore FROM speed_test_results")
        speedCursor.moveToFirst()
        
        assertEquals("test_result", speedCursor.getString(0))
        assertEquals(25, speedCursor.getInt(1)) // ping preserved
        
        // New columns should have default values
        assertEquals(0, speedCursor.getInt(2)) // bufferbloat default 0
        assertEquals(100, speedCursor.getInt(3)) // privacyScore default 100
        
        speedCursor.close()
        database.close()
    }

    @Test
    fun verifyMigrationIntegrity() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        var db = Room.databaseBuilder(
            context,
            PhotonDatabase::class.java,
            "migration_test_verify"
        ).addMigrations(MIGRATION_1_2).build()

        // Verify the database can be created and used with version 2 schema
        val server = DNSServer(
            id = "cloudflare",
            name = "Cloudflare",
            ip = "1.1.1.1",
            countryCode = "US",
            protocol = DNSProtocol.DOH,
            dohUrl = "https://cloudflare-dns.com/dns-query"
        )
        
        db.dnsServerDao().insertServer(server)
        
        val retrieved = db.dnsServerDao().getServerById("cloudflare")
        assertNotNull(retrieved)
        assertEquals(DNSProtocol.DOH, retrieved.protocol)
        assertEquals("https://cloudflare-dns.com/dns-query", retrieved.dohUrl)
        
        db.close()
    }
}
package com.photondns.app.data.repository;

import com.photondns.app.data.database.DNSServerDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class DNSServerRepository_Factory implements Factory<DNSServerRepository> {
  private final Provider<DNSServerDao> dnsServerDaoProvider;

  public DNSServerRepository_Factory(Provider<DNSServerDao> dnsServerDaoProvider) {
    this.dnsServerDaoProvider = dnsServerDaoProvider;
  }

  @Override
  public DNSServerRepository get() {
    return newInstance(dnsServerDaoProvider.get());
  }

  public static DNSServerRepository_Factory create(Provider<DNSServerDao> dnsServerDaoProvider) {
    return new DNSServerRepository_Factory(dnsServerDaoProvider);
  }

  public static DNSServerRepository newInstance(DNSServerDao dnsServerDao) {
    return new DNSServerRepository(dnsServerDao);
  }
}

package com.photondns.app.service;

import com.photondns.app.data.repository.DNSServerRepository;
import com.photondns.app.data.repository.LatencyRepository;
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
public final class DNSSwitchManager_Factory implements Factory<DNSSwitchManager> {
  private final Provider<DNSServerRepository> dnsServerRepositoryProvider;

  private final Provider<LatencyRepository> latencyRepositoryProvider;

  private final Provider<DNSLatencyChecker> dnsLatencyCheckerProvider;

  public DNSSwitchManager_Factory(Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<LatencyRepository> latencyRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    this.dnsServerRepositoryProvider = dnsServerRepositoryProvider;
    this.latencyRepositoryProvider = latencyRepositoryProvider;
    this.dnsLatencyCheckerProvider = dnsLatencyCheckerProvider;
  }

  @Override
  public DNSSwitchManager get() {
    return newInstance(dnsServerRepositoryProvider.get(), latencyRepositoryProvider.get(), dnsLatencyCheckerProvider.get());
  }

  public static DNSSwitchManager_Factory create(
      Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<LatencyRepository> latencyRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    return new DNSSwitchManager_Factory(dnsServerRepositoryProvider, latencyRepositoryProvider, dnsLatencyCheckerProvider);
  }

  public static DNSSwitchManager newInstance(DNSServerRepository dnsServerRepository,
      LatencyRepository latencyRepository, DNSLatencyChecker dnsLatencyChecker) {
    return new DNSSwitchManager(dnsServerRepository, latencyRepository, dnsLatencyChecker);
  }
}

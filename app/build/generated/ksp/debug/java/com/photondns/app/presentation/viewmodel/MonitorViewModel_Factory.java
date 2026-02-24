package com.photondns.app.presentation.viewmodel;

import com.photondns.app.data.repository.DNSServerRepository;
import com.photondns.app.data.repository.LatencyRepository;
import com.photondns.app.service.DNSLatencyChecker;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class MonitorViewModel_Factory implements Factory<MonitorViewModel> {
  private final Provider<DNSServerRepository> dnsServerRepositoryProvider;

  private final Provider<LatencyRepository> latencyRepositoryProvider;

  private final Provider<DNSLatencyChecker> dnsLatencyCheckerProvider;

  public MonitorViewModel_Factory(Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<LatencyRepository> latencyRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    this.dnsServerRepositoryProvider = dnsServerRepositoryProvider;
    this.latencyRepositoryProvider = latencyRepositoryProvider;
    this.dnsLatencyCheckerProvider = dnsLatencyCheckerProvider;
  }

  @Override
  public MonitorViewModel get() {
    return newInstance(dnsServerRepositoryProvider.get(), latencyRepositoryProvider.get(), dnsLatencyCheckerProvider.get());
  }

  public static MonitorViewModel_Factory create(
      Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<LatencyRepository> latencyRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    return new MonitorViewModel_Factory(dnsServerRepositoryProvider, latencyRepositoryProvider, dnsLatencyCheckerProvider);
  }

  public static MonitorViewModel newInstance(DNSServerRepository dnsServerRepository,
      LatencyRepository latencyRepository, DNSLatencyChecker dnsLatencyChecker) {
    return new MonitorViewModel(dnsServerRepository, latencyRepository, dnsLatencyChecker);
  }
}

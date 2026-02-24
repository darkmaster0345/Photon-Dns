package com.photondns.app.presentation.viewmodel;

import com.photondns.app.data.repository.DNSServerRepository;
import com.photondns.app.data.repository.LatencyRepository;
import com.photondns.app.data.repository.SpeedTestRepository;
import com.photondns.app.service.DNSLatencyChecker;
import com.photondns.app.service.DNSSwitchManager;
import com.photondns.app.service.SpeedTestManager;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<DNSServerRepository> dnsServerRepositoryProvider;

  private final Provider<LatencyRepository> latencyRepositoryProvider;

  private final Provider<SpeedTestRepository> speedTestRepositoryProvider;

  private final Provider<DNSLatencyChecker> dnsLatencyCheckerProvider;

  private final Provider<DNSSwitchManager> dnsSwitchManagerProvider;

  private final Provider<SpeedTestManager> speedTestManagerProvider;

  public HomeViewModel_Factory(Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<LatencyRepository> latencyRepositoryProvider,
      Provider<SpeedTestRepository> speedTestRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider,
      Provider<DNSSwitchManager> dnsSwitchManagerProvider,
      Provider<SpeedTestManager> speedTestManagerProvider) {
    this.dnsServerRepositoryProvider = dnsServerRepositoryProvider;
    this.latencyRepositoryProvider = latencyRepositoryProvider;
    this.speedTestRepositoryProvider = speedTestRepositoryProvider;
    this.dnsLatencyCheckerProvider = dnsLatencyCheckerProvider;
    this.dnsSwitchManagerProvider = dnsSwitchManagerProvider;
    this.speedTestManagerProvider = speedTestManagerProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(dnsServerRepositoryProvider.get(), latencyRepositoryProvider.get(), speedTestRepositoryProvider.get(), dnsLatencyCheckerProvider.get(), dnsSwitchManagerProvider.get(), speedTestManagerProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<LatencyRepository> latencyRepositoryProvider,
      Provider<SpeedTestRepository> speedTestRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider,
      Provider<DNSSwitchManager> dnsSwitchManagerProvider,
      Provider<SpeedTestManager> speedTestManagerProvider) {
    return new HomeViewModel_Factory(dnsServerRepositoryProvider, latencyRepositoryProvider, speedTestRepositoryProvider, dnsLatencyCheckerProvider, dnsSwitchManagerProvider, speedTestManagerProvider);
  }

  public static HomeViewModel newInstance(DNSServerRepository dnsServerRepository,
      LatencyRepository latencyRepository, SpeedTestRepository speedTestRepository,
      DNSLatencyChecker dnsLatencyChecker, DNSSwitchManager dnsSwitchManager,
      SpeedTestManager speedTestManager) {
    return new HomeViewModel(dnsServerRepository, latencyRepository, speedTestRepository, dnsLatencyChecker, dnsSwitchManager, speedTestManager);
  }
}

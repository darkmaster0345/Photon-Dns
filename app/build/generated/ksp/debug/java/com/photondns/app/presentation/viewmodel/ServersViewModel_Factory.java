package com.photondns.app.presentation.viewmodel;

import com.photondns.app.data.repository.DNSServerRepository;
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
public final class ServersViewModel_Factory implements Factory<ServersViewModel> {
  private final Provider<DNSServerRepository> dnsServerRepositoryProvider;

  private final Provider<DNSLatencyChecker> dnsLatencyCheckerProvider;

  public ServersViewModel_Factory(Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    this.dnsServerRepositoryProvider = dnsServerRepositoryProvider;
    this.dnsLatencyCheckerProvider = dnsLatencyCheckerProvider;
  }

  @Override
  public ServersViewModel get() {
    return newInstance(dnsServerRepositoryProvider.get(), dnsLatencyCheckerProvider.get());
  }

  public static ServersViewModel_Factory create(
      Provider<DNSServerRepository> dnsServerRepositoryProvider,
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    return new ServersViewModel_Factory(dnsServerRepositoryProvider, dnsLatencyCheckerProvider);
  }

  public static ServersViewModel newInstance(DNSServerRepository dnsServerRepository,
      DNSLatencyChecker dnsLatencyChecker) {
    return new ServersViewModel(dnsServerRepository, dnsLatencyChecker);
  }
}

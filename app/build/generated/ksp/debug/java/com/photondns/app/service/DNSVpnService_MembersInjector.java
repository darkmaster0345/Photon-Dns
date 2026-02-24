package com.photondns.app.service;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DNSVpnService_MembersInjector implements MembersInjector<DNSVpnService> {
  private final Provider<DNSLatencyChecker> dnsLatencyCheckerProvider;

  public DNSVpnService_MembersInjector(Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    this.dnsLatencyCheckerProvider = dnsLatencyCheckerProvider;
  }

  public static MembersInjector<DNSVpnService> create(
      Provider<DNSLatencyChecker> dnsLatencyCheckerProvider) {
    return new DNSVpnService_MembersInjector(dnsLatencyCheckerProvider);
  }

  @Override
  public void injectMembers(DNSVpnService instance) {
    injectDnsLatencyChecker(instance, dnsLatencyCheckerProvider.get());
  }

  @InjectedFieldSignature("com.photondns.app.service.DNSVpnService.dnsLatencyChecker")
  public static void injectDnsLatencyChecker(DNSVpnService instance,
      DNSLatencyChecker dnsLatencyChecker) {
    instance.dnsLatencyChecker = dnsLatencyChecker;
  }
}

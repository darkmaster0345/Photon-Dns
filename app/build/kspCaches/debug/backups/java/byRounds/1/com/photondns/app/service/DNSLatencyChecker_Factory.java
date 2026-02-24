package com.photondns.app.service;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class DNSLatencyChecker_Factory implements Factory<DNSLatencyChecker> {
  @Override
  public DNSLatencyChecker get() {
    return newInstance();
  }

  public static DNSLatencyChecker_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DNSLatencyChecker newInstance() {
    return new DNSLatencyChecker();
  }

  private static final class InstanceHolder {
    private static final DNSLatencyChecker_Factory INSTANCE = new DNSLatencyChecker_Factory();
  }
}

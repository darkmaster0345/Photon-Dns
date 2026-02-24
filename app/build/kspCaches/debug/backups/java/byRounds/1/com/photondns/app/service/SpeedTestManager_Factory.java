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
public final class SpeedTestManager_Factory implements Factory<SpeedTestManager> {
  @Override
  public SpeedTestManager get() {
    return newInstance();
  }

  public static SpeedTestManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SpeedTestManager newInstance() {
    return new SpeedTestManager();
  }

  private static final class InstanceHolder {
    private static final SpeedTestManager_Factory INSTANCE = new SpeedTestManager_Factory();
  }
}

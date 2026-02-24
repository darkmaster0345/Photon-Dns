package com.photondns.app.data.repository;

import com.photondns.app.data.database.SpeedTestDao;
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
public final class SpeedTestRepository_Factory implements Factory<SpeedTestRepository> {
  private final Provider<SpeedTestDao> speedTestDaoProvider;

  public SpeedTestRepository_Factory(Provider<SpeedTestDao> speedTestDaoProvider) {
    this.speedTestDaoProvider = speedTestDaoProvider;
  }

  @Override
  public SpeedTestRepository get() {
    return newInstance(speedTestDaoProvider.get());
  }

  public static SpeedTestRepository_Factory create(Provider<SpeedTestDao> speedTestDaoProvider) {
    return new SpeedTestRepository_Factory(speedTestDaoProvider);
  }

  public static SpeedTestRepository newInstance(SpeedTestDao speedTestDao) {
    return new SpeedTestRepository(speedTestDao);
  }
}

package com.photondns.app.presentation.viewmodel;

import com.photondns.app.data.repository.SpeedTestRepository;
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
public final class SpeedTestViewModel_Factory implements Factory<SpeedTestViewModel> {
  private final Provider<SpeedTestManager> speedTestManagerProvider;

  private final Provider<SpeedTestRepository> speedTestRepositoryProvider;

  public SpeedTestViewModel_Factory(Provider<SpeedTestManager> speedTestManagerProvider,
      Provider<SpeedTestRepository> speedTestRepositoryProvider) {
    this.speedTestManagerProvider = speedTestManagerProvider;
    this.speedTestRepositoryProvider = speedTestRepositoryProvider;
  }

  @Override
  public SpeedTestViewModel get() {
    return newInstance(speedTestManagerProvider.get(), speedTestRepositoryProvider.get());
  }

  public static SpeedTestViewModel_Factory create(
      Provider<SpeedTestManager> speedTestManagerProvider,
      Provider<SpeedTestRepository> speedTestRepositoryProvider) {
    return new SpeedTestViewModel_Factory(speedTestManagerProvider, speedTestRepositoryProvider);
  }

  public static SpeedTestViewModel newInstance(SpeedTestManager speedTestManager,
      SpeedTestRepository speedTestRepository) {
    return new SpeedTestViewModel(speedTestManager, speedTestRepository);
  }
}

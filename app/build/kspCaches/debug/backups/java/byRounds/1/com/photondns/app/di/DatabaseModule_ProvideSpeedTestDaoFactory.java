package com.photondns.app.di;

import com.photondns.app.data.database.PhotonDatabase;
import com.photondns.app.data.database.SpeedTestDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideSpeedTestDaoFactory implements Factory<SpeedTestDao> {
  private final Provider<PhotonDatabase> databaseProvider;

  public DatabaseModule_ProvideSpeedTestDaoFactory(Provider<PhotonDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SpeedTestDao get() {
    return provideSpeedTestDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSpeedTestDaoFactory create(
      Provider<PhotonDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSpeedTestDaoFactory(databaseProvider);
  }

  public static SpeedTestDao provideSpeedTestDao(PhotonDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSpeedTestDao(database));
  }
}

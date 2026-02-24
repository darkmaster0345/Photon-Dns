package com.photondns.app.di;

import com.photondns.app.data.database.LatencyDao;
import com.photondns.app.data.database.PhotonDatabase;
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
public final class DatabaseModule_ProvideLatencyDaoFactory implements Factory<LatencyDao> {
  private final Provider<PhotonDatabase> databaseProvider;

  public DatabaseModule_ProvideLatencyDaoFactory(Provider<PhotonDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public LatencyDao get() {
    return provideLatencyDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideLatencyDaoFactory create(
      Provider<PhotonDatabase> databaseProvider) {
    return new DatabaseModule_ProvideLatencyDaoFactory(databaseProvider);
  }

  public static LatencyDao provideLatencyDao(PhotonDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLatencyDao(database));
  }
}

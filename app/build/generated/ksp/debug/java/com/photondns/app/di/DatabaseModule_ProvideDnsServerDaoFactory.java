package com.photondns.app.di;

import com.photondns.app.data.database.DNSServerDao;
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
public final class DatabaseModule_ProvideDnsServerDaoFactory implements Factory<DNSServerDao> {
  private final Provider<PhotonDatabase> databaseProvider;

  public DatabaseModule_ProvideDnsServerDaoFactory(Provider<PhotonDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DNSServerDao get() {
    return provideDnsServerDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideDnsServerDaoFactory create(
      Provider<PhotonDatabase> databaseProvider) {
    return new DatabaseModule_ProvideDnsServerDaoFactory(databaseProvider);
  }

  public static DNSServerDao provideDnsServerDao(PhotonDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDnsServerDao(database));
  }
}

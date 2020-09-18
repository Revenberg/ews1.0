package org.hibernate.dialect;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataBuilderInitializer;
import org.hibernate.engine.jdbc.dialect.internal.DialectResolverSet;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SQLiteMetadataBuilderInitializer implements MetadataBuilderInitializer {

	protected final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void contribute(MetadataBuilder metadataBuilder, StandardServiceRegistry serviceRegistry) {
		DialectResolver dialectResolver = serviceRegistry.getService(DialectResolver.class);

		if (!(dialectResolver instanceof DialectResolverSet)) {						
			log.warn("DialectResolver '%s' is not an instance of DialectResolverSet, not registering SQLiteDialect",
					dialectResolver);
			return;
		}

		((DialectResolverSet) dialectResolver).addResolver(resolver);
	}

	static private final SQLiteDialect dialect = new SQLiteDialect();

	static private final DialectResolver resolver = new DialectResolver() {
		private static final long serialVersionUID = 1L;

		@Override
		public Dialect resolveDialect(DialectResolutionInfo info) {
			if (info.getDatabaseName().equals("SQLite"))
				return dialect;

			return null;
		}

	};
}

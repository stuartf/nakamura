package org.drools.repository.migration;

import javax.jcr.Session;

import org.drools.repository.AssetItem;
import org.drools.repository.PackageItem;
import org.drools.repository.RepositorySessionUtil;
import org.drools.repository.RulesRepository;

import junit.framework.TestCase;

public class MigrateDroolsPackageTest extends TestCase {

	public void testMigrate() throws Exception {
		RulesRepository repo = RepositorySessionUtil.getRepository();
		Session sess = repo.getSession();
		sess.getRootNode().getNode(RulesRepository.RULES_REPOSITORY_NAME).getNode("drools.package.migrated").remove();
		sess.save();

		MigrateDroolsPackage mig = new MigrateDroolsPackage();

		PackageItem pkg = repo.createPackage("testMigratePackage", "");
		pkg.updateStringProperty("some header", PackageItem.HEADER_PROPERTY_NAME);
		sess.save();

		repo.createPackageSnapshot("testMigratePackage", "SNAP1");
		repo.createPackageSnapshot("testMigratePackage", "SNAP2");



		assertTrue(mig.needsMigration(repo));
		mig.migrate(repo);
		assertFalse(repo.getSession().hasPendingChanges());
		assertFalse(mig.needsMigration(repo));
		pkg = repo.loadPackage("testMigratePackage");
		assertTrue(pkg.containsAsset("drools"));
		AssetItem as = pkg.loadAsset("drools");
		assertEquals("some header", as.getContent());


		pkg = repo.loadPackageSnapshot("testMigratePackage", "SNAP1");
		assertTrue(pkg.containsAsset("drools"));
		as = pkg.loadAsset("drools");
		assertEquals("some header", as.getContent());

		pkg = repo.loadPackageSnapshot("testMigratePackage", "SNAP2");
		assertTrue(pkg.containsAsset("drools"));
		as = pkg.loadAsset("drools");
		assertEquals("some header", as.getContent());




	}

}

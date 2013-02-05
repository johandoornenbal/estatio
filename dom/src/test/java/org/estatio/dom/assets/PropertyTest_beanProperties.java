package org.estatio.dom.assets;

import org.estatio.dom.FixtureDatumFactoriesForJoda;
import org.estatio.dom.asset.Property;
import org.junit.Test;

import com.danhaywood.testsupport.coverage.PojoTester;
import com.danhaywood.testsupport.coverage.PojoTester.FilterSet;

public class PropertyTest_beanProperties {

	@Test
	public void test() {
		new PojoTester()
		    .withFixture(FixtureDatumFactoriesForAssets.properties())
		    .withFixture(FixtureDatumFactoriesForAssets.units())
		    .withFixture(FixtureDatumFactoriesForAssets.propertyActors())
			.withFixture(FixtureDatumFactoriesForJoda.dates())
			.exercise(new Property(), FilterSet.excluding("container","units","propertyActorsRepo", "properties", "unitsRepo", "actors", "parties", "communicationChannels" ));
	}

}

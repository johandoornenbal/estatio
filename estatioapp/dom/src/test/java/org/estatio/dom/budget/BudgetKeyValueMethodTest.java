/*
 * Copyright 2015 Yodo Int. Projects and Consultancy
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.estatio.dom.budget;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;

import org.estatio.app.budget.IdentifierValueInputPair;
import org.estatio.app.budget.IdentifierValuesOutputObject;

import static org.assertj.core.api.Assertions.assertThat;

public class BudgetKeyValueMethodTest {

    @Test
    public void testCalculateDefault() {

        BudgetKeyValueMethod method = BudgetKeyValueMethod.DEFAULT;
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(1))).isEqualTo(new BigDecimal(1));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(10))).isEqualTo(new BigDecimal("0.1"));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(100))).isEqualTo(new BigDecimal("0.01"));
    }

    @Test
    public void testCalculateThousand() {

        BudgetKeyValueMethod method = BudgetKeyValueMethod.PROMILLE;
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(1))).isEqualTo(new BigDecimal(1000));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(10))).isEqualTo(new BigDecimal(100));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(100))).isEqualTo(new BigDecimal(10));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(1000))).isEqualTo(new BigDecimal(1));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(10000))).isEqualTo(new BigDecimal(0.1).setScale(1, BigDecimal.ROUND_HALF_UP));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(100000))).isEqualTo(new BigDecimal(0.01).setScale(2, BigDecimal.ROUND_HALF_UP));

    }

    @Test
    public void testCalculateHundred() {

        BudgetKeyValueMethod method = BudgetKeyValueMethod.PERCENT;
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(1))).isEqualTo(new BigDecimal(100));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(10))).isEqualTo(new BigDecimal(10));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(100))).isEqualTo(new BigDecimal(1));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(1000))).isEqualTo(new BigDecimal(0.1).setScale(1, BigDecimal.ROUND_HALF_UP));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(10000))).isEqualTo(new BigDecimal(0.01).setScale(2, BigDecimal.ROUND_HALF_UP));
        assertThat(method.calculate(new BigDecimal(1), new BigDecimal(100000))).isEqualTo(new BigDecimal(0.001).setScale(3, BigDecimal.ROUND_HALF_UP));

    }

    @Test
    public void testIsNotValid() {

        //given
        BudgetKeyValueMethod method = BudgetKeyValueMethod.PROMILLE;
        BudgetKeyTable budgetKeyTable = new BudgetKeyTable();
        SortedSet<BudgetKeyItem> budgetKeyItems = new TreeSet<BudgetKeyItem>();

        //when

        for(int i = 0; i < 1; i = i+1) {
            BudgetKeyItem budgetKeyItem = new BudgetKeyItem();
            budgetKeyItem.setKeyValue(new BigDecimal(999.999));
            budgetKeyItems.add(budgetKeyItem);
        }
        budgetKeyTable.setBudgetKeyItems(budgetKeyItems);

        // then
        assertThat(method.keySum(budgetKeyTable)).isEqualTo(new BigDecimal(999.999).setScale(3, BigDecimal.ROUND_HALF_UP));
        assertThat(method.isValid(budgetKeyTable)).isEqualTo(false);


    }

    @Test
    public void testIsValid() {

        //given
        BudgetKeyValueMethod method = BudgetKeyValueMethod.PROMILLE;
        BudgetKeyTable budgetKeyTable = new BudgetKeyTable();
        SortedSet<BudgetKeyItem> budgetKeyItems = new TreeSet<BudgetKeyItem>();

        //when

        for(int i = 0; i < 1; i = i+1) {
                        BudgetKeyItem budgetKeyItem = new BudgetKeyItem();
                        budgetKeyItem.setKeyValue(new BigDecimal(999.9999));
                        budgetKeyItems.add(budgetKeyItem);
                    }
        budgetKeyTable.setBudgetKeyItems(budgetKeyItems);

        // then
        assertThat(method.keySum(budgetKeyTable)).isEqualTo(new BigDecimal(1000).setScale(3, BigDecimal.ROUND_HALF_UP));
        assertThat(method.isValid(budgetKeyTable)).isEqualTo(true);

    }

    @Test
    public void testIsAlsoValid() {

        //given
        BudgetKeyValueMethod method = BudgetKeyValueMethod.PROMILLE;
        BudgetKeyTable budgetKeyTable = new BudgetKeyTable();
        SortedSet<BudgetKeyItem> budgetKeyItems = new TreeSet<BudgetKeyItem>();

        //when

        for(int i = 0; i < 1; i = i+1) {
            BudgetKeyItem budgetKeyItem = new BudgetKeyItem();
            budgetKeyItem.setKeyValue(new BigDecimal(1000.0001));
            budgetKeyItems.add(budgetKeyItem);
        }
        budgetKeyTable.setBudgetKeyItems(budgetKeyItems);

        // then
        assertThat(method.keySum(budgetKeyTable)).isEqualTo(new BigDecimal(1000).setScale(3, BigDecimal.ROUND_HALF_UP));
        assertThat(method.isValid(budgetKeyTable)).isEqualTo(true);

    }

    @Test
    public void generateKeyValuesWithoutCorrection() {

        //given
        BudgetKeyValueMethod method = BudgetKeyValueMethod.PROMILLE;
        ArrayList<IdentifierValueInputPair> input = new ArrayList<IdentifierValueInputPair>();
        IdentifierValueInputPair pair1 = new IdentifierValueInputPair(1, new BigDecimal(3344.66));
        IdentifierValueInputPair pair2 = new IdentifierValueInputPair(2, new BigDecimal(1122.99));
        IdentifierValueInputPair pair3 = new IdentifierValueInputPair(3, new BigDecimal(11.99));
        input.add(pair1);
        input.add(pair2);
        input.add(pair3);

        //when

        ArrayList<IdentifierValuesOutputObject> output = method.generateKeyValues(input);
        BigDecimal sumRoundedValues = BigDecimal.ZERO;
        for (IdentifierValuesOutputObject object: output) {
            sumRoundedValues = sumRoundedValues.add(object.getRoundedValue());
        }
        BigDecimal sumUnroundedValues = BigDecimal.ZERO;
        for (IdentifierValuesOutputObject object: output) {
            sumUnroundedValues = sumUnroundedValues.add(object.getValue());
        }

        //then
        assertThat(output.size()).isEqualTo(3);
        assertThat(output.get(0).getValue()).isEqualTo(new BigDecimal(746.6359).setScale(4, BigDecimal.ROUND_HALF_UP));
        assertThat(output.get(0).getRoundedValue()).isEqualTo(new BigDecimal(746.636).setScale(3, BigDecimal.ROUND_HALF_UP));
        assertThat(output.get(1).getValue()).isEqualTo(new BigDecimal(250.6876).setScale(4, BigDecimal.ROUND_HALF_UP));
        assertThat(output.get(1).getRoundedValue()).isEqualTo(new BigDecimal(250.688).setScale(3, BigDecimal.ROUND_HALF_UP));
        assertThat(output.get(2).getValue()).isEqualTo(new BigDecimal(2.676554).setScale(6, BigDecimal.ROUND_HALF_UP));
        assertThat(output.get(2).getRoundedValue()).isEqualTo(new BigDecimal(2.677).setScale(3, BigDecimal.ROUND_HALF_UP));

        // Rounding Error for 3 decimals
        assertThat(sumRoundedValues.setScale(3, BigDecimal.ROUND_HALF_UP)).isEqualTo(new BigDecimal(1000.001).setScale(3, BigDecimal.ROUND_HALF_UP));
        assertThat(sumUnroundedValues.setScale(3, BigDecimal.ROUND_HALF_UP)).isEqualTo(new BigDecimal(1000.000).setScale(3, BigDecimal.ROUND_HALF_UP));
    }

}

/*
 *
 *  Copyright 2012-2015 Eurocommercial Properties NV
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.estatio.dom.budget;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;

import org.estatio.app.budget.IdentifierValueInputPair;
import org.estatio.app.budget.IdentifierValuesOutputObject;

public enum BudgetKeyValueMethod {
    PROMILLE {
        @Override
        public BigDecimal calculate(final BigDecimal numerator, final BigDecimal denominator) {
            return numerator.multiply(new BigDecimal(1000), MathContext.DECIMAL32).divide(denominator, MathContext.DECIMAL32);
        }
        @Override
        public boolean isValid(BudgetKeyTable budgetKeyTable) {
                if (!this.keySum(budgetKeyTable).equals(new BigDecimal(1000.000).setScale(3,BigDecimal.ROUND_HALF_UP))) {
                    return false;
                }
            return true;
        }
        @Override
        public BigDecimal keySum(BudgetKeyTable budgetKeyTable) {
            BigDecimal sum = BigDecimal.ZERO;
            for (Iterator<BudgetKeyItem> it = budgetKeyTable.getBudgetKeyItems().iterator(); it.hasNext();) {
                sum = sum.add(it.next().getKeyValue());
            }
            return sum.setScale(3, BigDecimal.ROUND_HALF_UP);
        }
        @Override
        public BudgetKeyTable correctKeyTable(BudgetKeyTable budgetKeyTable){
            if(keySum(budgetKeyTable).compareTo(new BigDecimal(1000)) > 0) {

                System.out.println("Positive delta found in sum of key values: ");
                System.out.println(keySum(budgetKeyTable));
                System.out.println("Finding largest negative delta and round down keyvalue");

                //Find largest negative delta and round down keyvalue

                BigDecimal largestNegativeDelta = BigDecimal.ZERO;
                BudgetKeyItem itemToRoundDown = null;
                for (Iterator<BudgetKeyItem> it = budgetKeyTable.getBudgetKeyItems().iterator(); it.hasNext();) {
                    BudgetKeyItem item = it.next();
                    if(item.delta().compareTo(largestNegativeDelta)<0){
                        itemToRoundDown = item;
                        System.out.println(itemToRoundDown.delta());
                        System.out.println(itemToRoundDown.getKeyValue());
                    }
                }

                itemToRoundDown.changeKeyValue(itemToRoundDown.getKeyValue().subtract(new BigDecimal(0.001)));
                System.out.println("New keyValue");
                System.out.println(itemToRoundDown.getKeyValue());

            }

            if(keySum(budgetKeyTable).compareTo(new BigDecimal(1000)) < 0) {

                System.out.println("Negative delta found in sum of key values: ");
                System.out.println(keySum(budgetKeyTable));
                System.out.println("Finding largest positive delta and round up keyvalue");

                //Find largest positive delta and round up keyvalue

                BigDecimal largestDelta = BigDecimal.ZERO;
                BudgetKeyItem itemToRoundUp = null;
                for (Iterator<BudgetKeyItem> it = budgetKeyTable.getBudgetKeyItems().iterator(); it.hasNext();) {
                    BudgetKeyItem item = it.next();
                    if(item.delta().compareTo(largestDelta)>0){
                        itemToRoundUp = item;
                        System.out.println(itemToRoundUp.delta());
                        System.out.println(itemToRoundUp.getKeyValue());
                    }
                }

                itemToRoundUp.changeKeyValue(itemToRoundUp.getKeyValue().add(new BigDecimal(0.001)));
                System.out.println("New keyValue");
                System.out.println(itemToRoundUp.getKeyValue());

            }

            return budgetKeyTable;
        }
        @Override
        public ArrayList<IdentifierValuesOutputObject> generateKeyValues(final ArrayList<IdentifierValueInputPair> input) {
            BigDecimal denominator = BigDecimal.ZERO;
            for (IdentifierValueInputPair pair : input) {
                denominator = denominator.add(pair.getValue());
            }

            // check if rounding correction is needed
            BigDecimal sumOfCalculatedRoundedValues = BigDecimal.ZERO;
            boolean correctionNeeded = false;
            BigDecimal deltaOfSum = BigDecimal.ZERO;
            BigDecimal validTotal = new BigDecimal(1000.000).setScale(3, BigDecimal.ROUND_HALF_UP);
            for (IdentifierValueInputPair inputPair : input) {
                BigDecimal keyValue = calculate(inputPair.getValue(), denominator);
                BigDecimal roundedKeyValue = keyValue.setScale(3, BigDecimal.ROUND_HALF_UP);
                sumOfCalculatedRoundedValues = sumOfCalculatedRoundedValues.add(roundedKeyValue);
            }
            if (sumOfCalculatedRoundedValues.compareTo(validTotal) > 0) {
                correctionNeeded = true;
                System.out.println("positive delta: ");
                deltaOfSum = deltaOfSum.add(sumOfCalculatedRoundedValues.subtract(validTotal));
                System.out.println(deltaOfSum);
            }
            if (sumOfCalculatedRoundedValues.compareTo(validTotal) < 0) {
                correctionNeeded = true;
                System.out.println("negative delta: ");
                deltaOfSum = deltaOfSum.add(sumOfCalculatedRoundedValues.subtract(validTotal));
                System.out.println(deltaOfSum);
            }
            if (sumOfCalculatedRoundedValues.compareTo(validTotal) == 0) {
                correctionNeeded = false;
                System.out.println("no delta ");
            }

            ArrayList<IdentifierValuesOutputObject> output = new ArrayList<IdentifierValuesOutputObject>();
            for (IdentifierValueInputPair inputPair : input) {
                BigDecimal keyValue = calculate(inputPair.getValue(), denominator);
                BigDecimal roundedKeyValue = keyValue.setScale(3, BigDecimal.ROUND_HALF_UP);
                IdentifierValuesOutputObject newOutputObject = new IdentifierValuesOutputObject(
                        inputPair.getIdentifier(),
                        keyValue,
                        roundedKeyValue,
                        roundedKeyValue.subtract(keyValue)
                );
                output.add(newOutputObject);
            }

            return output;
        }
    },
    PERCENT {
        @Override
        public BigDecimal calculate(final BigDecimal numerator, final BigDecimal denominator) {
            return numerator.multiply(new BigDecimal(100), MathContext.DECIMAL32).divide(denominator, MathContext.DECIMAL32);
        }
        @Override
        public boolean isValid(BudgetKeyTable budgetKeyTable) {
            if (!this.keySum(budgetKeyTable).equals(new BigDecimal(100.000).setScale(3, BigDecimal.ROUND_HALF_UP))) {
                return false;
            }
            return true;
        }
        @Override
        public BigDecimal keySum(BudgetKeyTable budgetKeyTable) {
            BigDecimal sum = BigDecimal.ZERO;
            for (Iterator<BudgetKeyItem> it = budgetKeyTable.getBudgetKeyItems().iterator(); it.hasNext();) {
                sum = sum.add(it.next().getKeyValue());
            }
            return sum.setScale(3,BigDecimal.ROUND_HALF_UP);
        }
        @Override
        public BudgetKeyTable correctKeyTable(BudgetKeyTable budgetKeyTable){

            return budgetKeyTable;
        }
        @Override
        public ArrayList<IdentifierValuesOutputObject> generateKeyValues(final ArrayList<IdentifierValueInputPair> input) {
            BigDecimal denominator = BigDecimal.ZERO;
            for (IdentifierValueInputPair pair : input) {
                denominator = denominator.add(pair.getValue());
            }

            ArrayList<IdentifierValuesOutputObject> output = new ArrayList<IdentifierValuesOutputObject>();
            for (IdentifierValueInputPair inputPair : input) {
                BigDecimal keyValue = calculate(inputPair.getValue(), denominator);
                BigDecimal roundedKeyValue = keyValue.setScale(3, BigDecimal.ROUND_HALF_UP);
                IdentifierValuesOutputObject newOutputObject = new IdentifierValuesOutputObject(
                        inputPair.getIdentifier(),
                        keyValue,
                        roundedKeyValue,
                        roundedKeyValue.subtract(keyValue)
                );
                output.add(newOutputObject);
            }

            return output;
        }
    },
    DEFAULT {
        @Override
        public BigDecimal calculate(final BigDecimal numerator, final BigDecimal denominator) {
            return numerator.divide(denominator, MathContext.DECIMAL32);
        }
        @Override
        public boolean isValid(BudgetKeyTable budgetKeyTable) {
            return true;
        }
        @Override
        public BigDecimal keySum(BudgetKeyTable budgetKeyTable) {
            BigDecimal sum = BigDecimal.ZERO;
            for (Iterator<BudgetKeyItem> it = budgetKeyTable.getBudgetKeyItems().iterator(); it.hasNext();) {
                sum = sum.add(it.next().getKeyValue());
            }
            return sum;
        }
        @Override
        public BudgetKeyTable correctKeyTable(BudgetKeyTable budgetKeyTable){

            return budgetKeyTable;
        }
        @Override
        public ArrayList<IdentifierValuesOutputObject> generateKeyValues(final ArrayList<IdentifierValueInputPair> input) {
            BigDecimal denominator = BigDecimal.ZERO;
            for (IdentifierValueInputPair pair : input) {
                denominator = denominator.add(pair.getValue());
            }

            ArrayList<IdentifierValuesOutputObject> output = new ArrayList<IdentifierValuesOutputObject>();
            for (IdentifierValueInputPair inputPair : input) {
                BigDecimal keyValue = calculate(inputPair.getValue(), denominator);
                BigDecimal roundedKeyValue = keyValue.setScale(3, BigDecimal.ROUND_HALF_UP);
                IdentifierValuesOutputObject newOutputObject = new IdentifierValuesOutputObject(
                        inputPair.getIdentifier(),
                        keyValue,
                        roundedKeyValue,
                        roundedKeyValue.subtract(keyValue)
                );
                output.add(newOutputObject);
            }

            return output;
        }
    };

    public abstract BigDecimal calculate(final BigDecimal numerator, final BigDecimal denominator);

    public abstract ArrayList<IdentifierValuesOutputObject> generateKeyValues(final ArrayList<IdentifierValueInputPair> input);

    public abstract boolean isValid(final BudgetKeyTable budgetKeyTable);

    public abstract BigDecimal keySum(final BudgetKeyTable budgetKeyTable);

    public abstract BudgetKeyTable correctKeyTable(final BudgetKeyTable budgetKeyTable);
}

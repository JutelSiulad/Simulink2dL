package simulink2dl.transform.optimizer;

import java.util.List;

import simulink2dl.dlmodel.hybridprogram.HybridProgram;
import simulink2dl.dlmodel.hybridprogram.HybridProgramCollection;
import simulink2dl.dlmodel.hybridprogram.TestFormula;
import simulink2dl.dlmodel.operator.formula.Formula;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalChoice;
import simulink2dl.transform.dlmodel.hybridprogram.ConditionalHybridProgram;

/**
 * Optimizer for removing unnecessary subprogram from HybridProgramCollections
 * @author Julius Adelt
 *
 */
public abstract class CollectionOptimization {

	protected static void optimize(HybridProgramCollection hpCollection) {
		
		simplifyTriviallyTrueConditionalProgram(hpCollection);
		
		simplifyTriviallyTrueConditionalChoice(hpCollection);
		
		removeTrivialTests(hpCollection);
		
		trimCollection(hpCollection);
		
		mergeConditionalHybridPrograms(hpCollection);
		
	}
		
	//merges successive conditional choice programs if tests are equal TODO: check if bound variables are influenced
	private static void mergeConditionalHybridPrograms(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for(int i = 0; i<hps.size()-1;i++) {
			HybridProgram currentHP = hps.get(i);
			HybridProgram nextHP = hps.get(i+1);
			if(currentHP instanceof ConditionalChoice && nextHP instanceof ConditionalChoice) {
				ConditionalChoice currentConditionalChoice = (ConditionalChoice) currentHP;
				ConditionalChoice nextConditionalChoice = (ConditionalChoice) nextHP;
				List<ConditionalHybridProgram> currentChoices = currentConditionalChoice.getChoices();
				List<ConditionalHybridProgram> nextChoices = nextConditionalChoice.getChoices();
				boolean equal = true;
				if(!(currentChoices.size()==nextChoices.size())) {
					equal = false;
				} else {
					for(int j = 0; j<currentChoices.size(); j++) {
						ConditionalHybridProgram currentChoice = currentChoices.get(j);
						ConditionalHybridProgram nextChoice = nextChoices.get(j);
						if(!currentChoice.getCondition().equals(nextChoice.getCondition())) {
							equal = false;
						}
					}
				}
				if(equal) {
					for(int j = 0; j<currentChoices.size(); j++) {
						ConditionalHybridProgram currentChoice = currentChoices.get(j);
						ConditionalHybridProgram nextChoice = nextChoices.get(j);
						HybridProgramCollection newHP = new HybridProgramCollection();
						newHP.addElement(currentChoice.getInnerProgram());
						newHP.addElement(nextChoice.getInnerProgram());
						trimCollection(newHP);
						currentChoice.setInnerProgram(newHP);
					}
					hps.remove(i+1);
					i--;
				}
			}
		}
	}
	
	private static void removeTrivialTests(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for(int i = 0; i<hps.size();i++) {
			HybridProgram hp = hps.get(i);
			if(hp instanceof TestFormula) {
				Formula test = ((TestFormula)hp).getFormula();
				if(FormulaOptimization.isTrue(test)) {
					hps.remove(i);
					i--;
				}
			}
		}
	}
	
	private static void simplifyTriviallyTrueConditionalProgram(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for (int i = 0; i<hps.size(); i++) {
			HybridProgram hp = hps.get(i);
			if(hp instanceof ConditionalHybridProgram) {
				if(FormulaOptimization.isTrue(((ConditionalHybridProgram)hp).getCondition())) {
					hps.set(i, ((ConditionalHybridProgram)hp).getInnerProgram());
				}
			}
		}
	}
	
	private static void simplifyTriviallyTrueConditionalChoice(HybridProgramCollection hpCollection) {
		List<HybridProgram> hps = hpCollection.getSequence();
		for (int i = 0; i<hps.size(); i++) {
			HybridProgram hp = hps.get(i);
			if(hp instanceof ConditionalChoice) {
				ConditionalChoice condChoice = (ConditionalChoice)hp;
				if(condChoice.getChoices().size()==1) {
					ConditionalHybridProgram onlyChoice = condChoice.getChoices().get(0);
					if(FormulaOptimization.isTrue(onlyChoice.getCondition())) {
						hps.set(i, onlyChoice.getInnerProgram());
					}
				}
			}
		}
	}
	
	private static void trimCollection(HybridProgramCollection hpCollection) {
		List<HybridProgram> collectionSequence = hpCollection.getSequence();
//		boolean change = false;
//		
//		do {
//			change = false;
		for(int i = 0; i<collectionSequence.size(); i++) {
			if(collectionSequence.get(i) instanceof HybridProgramCollection) {
				List<HybridProgram> nestedSequence = ((HybridProgramCollection)collectionSequence.get(i)).getSequence();
				if(nestedSequence.size()==1) {
					collectionSequence.set(i, nestedSequence.get(0));
//						change=true;
				}
			}
		}
		
		for(int i = 0; i<collectionSequence.size(); i++) {
			HybridProgram hp = collectionSequence.get(i);
			if(hp instanceof HybridProgramCollection) {
				HybridProgramCollection innerProgram = (HybridProgramCollection) hp;
				if(innerProgram.isEmpty()) {
					collectionSequence.remove(i);
					i--;
//						change=true;
				}
			}
		}
//		} while (change);

	}
	

}

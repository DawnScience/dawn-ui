package org.dawnsci.common.widgets.periodictable;
import java.util.List;

public class PeriodicTable {

	private PeriodicTable() {
	}

	// shamelessly copied from xraylib as this allows us to use
	// SymbolToAtomicNumber and AtomicNumberToSymbol without pulling
	// in the entire jar
	private static final List<String> MendelArray = List.of("",
		"H", "He", "Li", "Be", "B", "C", "N", "O", "F", "Ne",
		"Na", "Mg", "Al", "Si", "P", "S", "Cl", "Ar", "K", "Ca",
		"Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn",
		"Ga", "Ge", "As", "Se", "Br", "Kr", "Rb", "Sr", "Y", "Zr",
		"Nb", "Mo", "Tc", "Ru", "Rh", "Pd", "Ag", "Cd", "In", "Sn",
		"Sb", "Te", "I", "Xe", "Cs", "Ba", "La", "Ce", "Pr", "Nd",
		"Pm", "Sm", "Eu", "Gd", "Tb", "Dy", "Ho", "Er", "Tm", "Yb",
		"Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Hg",
		"Tl", "Pb", "Bi", "Po", "At", "Rn", "Fr", "Ra", "Ac", "Th",
		"Pa", "U", "Np", "Pu", "Am", "Cm", "Bk", "Cf", "Es", "Fm",
		"Md", "No", "Lr", "Rf", "Db", "Sg", "Bh"
	);

	public static List<String> getMendelArray() {
		return MendelArray;
	}

	public static String atomicNumberToSymbol(int z) {
		return MendelArray.get(z);
	}

	public static int symbolToAtomicNumber(String symbol) {
		return MendelArray.indexOf(symbol);
	}
}
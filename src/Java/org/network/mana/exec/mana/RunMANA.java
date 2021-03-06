package Java.org.network.mana.exec.mana;

import Java.org.network.mana.functions.StructuralPlasticity;
import Java.org.network.mana.globals.Default_Parameters;
import Java.org.network.mana.io.MANAWriter;
import Java.org.network.mana.mana_components.MANA_Unit;

import java.io.File;

import static Java.org.network.mana.globals.Default_Parameters.dt;

public class RunMANA {

	public static final String DEF_ODIR = "."+File.separator+"Outputs" + File.separator;
	public static final String DEF_PREFIX = "MANA";
	public static final double DEF_PRINT_INTERVAL = 6E5;

	public static void main(String[] args) { // TODO: enable more complicated command line args...
		System.out.println(System.getProperty("user.dir"));
		int numNeu = 2000;
		double time_f0 = 7.2E6; // two hours...
		double plastShutOff0 = time_f0/2;
		double spInterval = 10000;
		String filename = null;
		String odir = DEF_ODIR;
		String prefix = DEF_PREFIX;
        double printInterval = 1000;
        for (int ii = 0; ii < args.length; ++ii) {
            switch (args[ii]) {
                case "-n":
                    numNeu = Integer.parseInt(args[++ii]);
                    break;
                case "-f":
                    filename = args[++ii];
                    break;
                case "-plastOff":
                    plastShutOff0 = Integer.parseInt(args[++ii]);
                    break;
                case "-time":
                    time_f0 = Double.parseDouble(args[++ii]);
                    break;
                case "-label":
                    prefix = args[++ii];
                    break;
                case "-o":
                    odir = args[++ii];
                    break;
                case "-printInterval":
                    printInterval = Double.parseDouble(args[++ii]);
                    break;
                case "-spInterval":
                    spInterval = Double.parseDouble(args[++ii]);
                    break;
				case "-prune" :
					String val = args[++ii];
					if (val.equalsIgnoreCase("local")) {
						StructuralPlasticity.pruneTechnique = StructuralPlasticity.SPTechnique.LOCAL_MAX;
					} else if (val.equalsIgnoreCase("global")) {
						StructuralPlasticity.pruneTechnique = StructuralPlasticity.SPTechnique.GLOBAL_MAX;
					} else {
						throw new IllegalArgumentException("Unknown input.");
					}
					break;
				case "-pthresh":
					Default_Parameters.DEF_Thresh = Double.parseDouble(args[++ii]);
					break;
                default:
                    throw new IllegalArgumentException("Unknown input.");
            }
        }
		final double time_f = time_f0;
		final double p_shutOff_f = plastShutOff0;
		if(filename == null) {
			throw new IllegalArgumentException("No filename for input "
					+ "spikes was specified--exiting...");
		}
		MANA_Unit unit = MANA_Unit.MANABuilder(filename, numNeu);
		MANA_Executor exec = new MANA_Executor(spInterval); // initialize threads
		for(int ii=0, n=unit.nodes.size(); ii<n; ++ii) {
			if (ii % (int) Math.ceil(unit.nodes.size()/Math.sqrt(unit.nodes.size()))  == 0) {
				System.out.println();
			}
			System.out.print(unit.nodes.get(ii).type.isExcitatory() + " ");
		}
		System.out.println();
        double maxDist = unit.getMaxDist();
        double lambda = maxDist/2;
        double time = 0.0;
        boolean tripped = false;
		exec.addUnit(unit, unit.getFullSize(), unit.getSize(), lambda, maxDist); // tell them what unit they'll be working on
		unit.initialize(); // Set the various initial values that can only be set once weights/connectivity is known
		File mainOut = new File(odir);
		if (!mainOut.exists()) {
			if (!mainOut.mkdir()) {
				System.err.println("FATAL ERROR: FAILED TO CREATE OR FIND MAIN "
						+ "OUTPUT DIRECTORY.");
				System.exit(1);
			}
		}
		long iters = 0;
		boolean first = true;
		try {
			while(time < time_f) {
				if(time >= p_shutOff_f && !tripped) {
					System.out.println("Turning off plasticity");
					tripped = true;
					unit.setMhpOn(false);
					unit.setNormalizationOn(false);
					unit.setSynPlasticOn(false);
				}

				if(iters%((int)(1000/dt)) == 0) {
                    System.out.println((int)(iters*dt));
                }

				exec.invoke();

				if((iters)%(1000/ dt) == 0 && time != 0) {
					System.out.println("------------- " + time + "------------- " );
					if ((iters)%(50000/ dt) == 0 || first || iters == (int)(10000/dt) || iters == (int)(9000/dt)) {
						MANAWriter.printData2Matlab(unit, mainOut.toString(), prefix, time, dt);
						first = false;
					}
				}
				time = exec.getTime(); // get time from the executor
				iters++;
			}
		} catch (Exception ie) {
			ie.printStackTrace();
		} finally {
			MANAWriter.printData2Matlab(unit, mainOut.toString(), prefix, time, dt);
		}

	}

}



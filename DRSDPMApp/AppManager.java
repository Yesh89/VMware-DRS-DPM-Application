/**
 * 
 */
package DRSDPMApp;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Scanner;

import com.vmware.vim25.Description;
import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.FileFault;
import com.vmware.vim25.InsufficientResourcesFault;
import com.vmware.vim25.InvalidDatastore;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.OutOfBounds;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfEntityMetricCSV;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfMetricSeriesCSV;
import com.vmware.vim25.PerfProviderSummary;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineCloneSpec;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineRelocateSpec;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.VmConfigFault;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.PerformanceManager;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * @author admin
 *
 */
public class AppManager {
	
	String vcenter = "https://130.65.132.14/sdk";
	String uname = "administrator";
	String pwd = "12!@qwQW";
	Scanner input = new Scanner(System.in);
	ServiceInstance si;
	VirtualMachine vm;
	Folder rootFolder;
	InventoryNavigator inv;
	
	AppManager(){
		try {
			this.si = new ServiceInstance(new URL(vcenter), uname, pwd, true);
			rootFolder = si.getRootFolder();
			inv = new InventoryNavigator(si.getRootFolder());
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void createVM() throws Exception{
		//System.out.println("Enter name of the VM");
		VirtualMachineConfigSpec createSpec = new VirtualMachineConfigSpec();
		createSpec.setName("newVM");
		createSpec.setAnnotation("New Annotation");
		
		createSpec.setMemoryMB((long) 512);
		createSpec.setNumCPUs(1);
		createSpec.setGuestId("name2");
		
		VirtualDeviceConfigSpec scsiSpec = createScsiSpec(1000);
	    VirtualDeviceConfigSpec diskSpec = createDiskSpec("nfs1team15", 1000, 10000, "persistent");
	    VirtualDeviceConfigSpec nicSpec = createNicSpec("VM Network", "Network Adapter 1");
		
		
	    createSpec.setDeviceChange(new VirtualDeviceConfigSpec[]{scsiSpec, diskSpec, nicSpec});
	    
	    VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
	    vmfi.setVmPathName("[nfs1team15]");
	    createSpec.setFiles(vmfi);
	    //createSpec.set/************************************* Change here****************************//
		
		Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", "T15-Datacenter");
		
		ResourcePool rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntities("ResourcePool")[1];
		//ResourcePool rp = (ResourcePool) new InventoryNavigator(rootFolder).searchManagedEntities("ResourcePool")[0];
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities( new String[][] { {"HostSystem", "name" }, }, true);
		
		HostSystem host = (HostSystem) hosts[0];
		//System.out.println(rp.getName());
		//System.out.println(dc.getName());
		//System.out.println(host.getName());
		//Task newTask = rootFolder.createVM_Task(createSpec, rp, host);
		rootFolder.createVM_Task(createSpec, rp, host);
		/*
		ManagedEntity[] vms = new InventoryNavigator(rootFolder).searchManagedEntities(	new String[][] { {"VirtualMachine", "name" }, }, true);
		VirtualMachine vm = (VirtualMachine) vms[0];
		//System.out.println(vm.getSummary().getGuest().getIpAddress());
		VirtualMachineCloneSpec cloneSpec =	new VirtualMachineCloneSpec();
				cloneSpec.setLocation(new VirtualMachineRelocateSpec());
				cloneSpec.setPowerOn(true);
				cloneSpec.setTemplate(false);
				Task task = vm.cloneVM_Task((Folder) vm.getParent(),"New clone", cloneSpec);*/
	}

	
	static VirtualDeviceConfigSpec createScsiSpec(int cKey)
	{
		VirtualDeviceConfigSpec scsiSpec = new VirtualDeviceConfigSpec();
		scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
		scsiCtrl.setKey(cKey);
		scsiCtrl.setBusNumber(0);
		scsiCtrl.setSharedBus(VirtualSCSISharing.noSharing);
		scsiSpec.setDevice(scsiCtrl);
		return scsiSpec;
	}
	
	static VirtualDeviceConfigSpec createDiskSpec(String dsName, int cKey, long diskSizeKB, String diskMode)
	{
		VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
		diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);
		
		VirtualDisk vd = new VirtualDisk();
		vd.setCapacityInKB(diskSizeKB);
		diskSpec.setDevice(vd);
		vd.setKey(0);
		vd.setUnitNumber(0);
		vd.setControllerKey(cKey);
		
		VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
		String fileName = "["+ dsName +"]";
		diskfileBacking.setFileName(fileName);
		diskfileBacking.setDiskMode(diskMode);
		diskfileBacking.setThinProvisioned(true);
		vd.setBacking(diskfileBacking);
		return diskSpec;
	}
	
	static VirtualDeviceConfigSpec createNicSpec(String netName, String nicName) throws Exception
	{
		VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
		nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		
		VirtualEthernetCard nic =  new VirtualPCNet32();
		VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
		nicBacking.setDeviceName(netName);
		
		Description info = new Description();
		info.setLabel(nicName);
		info.setSummary(netName);
		nic.setDeviceInfo(info);
		nic.setAddressType("generated");
	    nic.setBacking(nicBacking);
	    nic.setKey(0);
	   
	    nicSpec.setDevice(nic);
	    return nicSpec;
	}
	
	public void getPerformance() throws RuntimeFault, RemoteException{
		String vmname = "T15-130.65.132.115";
		ManagedEntity VMe = null;
		try {
			VMe = new InventoryNavigator(si.getRootFolder()).searchManagedEntity("VirtualMachine", vmname);
		} catch (InvalidProperty e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(VMe == null)
		{
		System.out.println("Virtual Machine "+ vmname + " cannot be found.");
		si.getServerConnection().logout();
		return;
		}
		
		
		
		
		PerformanceManager perfMgr = si.getPerformanceManager();
		PerfProviderSummary pps = perfMgr.queryPerfProviderSummary(VMe);
		int refreshRate = pps.getRefreshRate().intValue();
		//System.out.println(refreshRate);
		
		
		
		
		PerfMetricId[] pmis = perfMgr.queryAvailablePerfMetric(VMe, null, null, refreshRate);
		
		PerfQuerySpec qSpec = createPerfQuerySpec(VMe, pmis, 1, refreshRate);
		
		
		PerfEntityMetricBase[] pValues = perfMgr.queryPerf(	new PerfQuerySpec[] {qSpec});
		if(pValues != null)
		{
		displayValues(pValues);
		}
		//System.out.println("Sleeping 60 seconds...");
	
		
		
		
		
		
		}
	
		static PerfQuerySpec createPerfQuerySpec(ManagedEntity me,PerfMetricId[] metricIds, int maxSample, int interval)
				{
				PerfQuerySpec qSpec = new PerfQuerySpec();
				qSpec.setEntity(me.getMOR());
				// set the maximum of metrics to be returned
				// only appropriate in real-time performance collecting
				qSpec.setMaxSample(new Integer(maxSample));
				qSpec.setMetricId(metricIds);
				// optionally you can set format as �normal�
				qSpec.setFormat("csv");
				// set the interval to the refresh rate for the entity
				qSpec.setIntervalId(new Integer(interval));
				return qSpec;
	}
				
		static void displayValues(PerfEntityMetricBase[] values){
			
		for(int i=0; i<values.length; ++i)
		{
				String entityDesc = values[i].getEntity().getType()	+ ":" + values[i].getEntity().get_value();
				System.out.println("Entity:" + entityDesc);
				
				if(values[i] instanceof PerfEntityMetric)		{
					//printPerfMetric((PerfEntityMetric)values[i]);
				}
				else if(values[i] instanceof PerfEntityMetricCSV)
				{
					printPerfMetricCSV((PerfEntityMetricCSV)values[i]);
				}
				else
				{
					System.out.println("UnExpected sub-type of " +	"PerfEntityMetricBase.");
				}
				
		}
		}
				
		static void printPerfMetric(PerfEntityMetric pem)
		{
		PerfMetricSeries[] vals = pem.getValue();
		PerfSampleInfo[] infos = pem.getSampleInfo();
		//PerfCounterInfo [] counterInfo ;
		
		System.out.println("Sampling Times and Intervales:");
		for(int i=0; infos!=null && i <infos.length; i++)
		{
		System.out.println("Sample time: "
		+ infos[i].getTimestamp().getTime());
		System.out.println("Sample interval (sec):"
		+ infos[i].getInterval());
		}
		System.out.println("Sample values:");
		for(int j=0; vals!=null && j<vals.length; ++j)
		{
		System.out.println("Perf counter ID:"
		+ vals[j].getId().getCounterId());
		System.out.println("Device instance ID:"
		+ vals[j].getId().getInstance());
		if(vals[j] instanceof PerfMetricIntSeries)
		{
		PerfMetricIntSeries val = (PerfMetricIntSeries) vals[j];
		long[] longs = val.getValue();
		for(int k=0; k<longs.length; k++)
		{
		System.out.print(longs[k] + " ");
		}
		System.out.println("Total:"+longs.length);
		}
		else if(vals[j] instanceof PerfMetricSeriesCSV)
		{ // it is not likely coming here...
		PerfMetricSeriesCSV val = (PerfMetricSeriesCSV) vals[j];
		System.out.println("CSV value:" + val.getValue());
		}
		}
		}
		
		static void printPerfMetricCSV(PerfEntityMetricCSV pems)
		{
		System.out.println("SampleInfoCSV:"+ pems.getSampleInfoCSV());
		PerfMetricSeriesCSV[] csvs = pems.getValue();
		System.out.println(csvs.length);
		//PerfCounterInfo[] pcis = csvs;
		for(int i=0; i<csvs.length; i++)
		{
		System.out.println("PerfCounterId:"+ csvs[i].getId().getCounterId());
		//System.out.println("CSV sample values:"+ csvs[i].getValue());
		System.out.println(csvs[i].getValue());
		
		
		System.out.println("*******************************");
		
		}
		}
		
		
		
		public Double getCPU() throws RuntimeFault, RemoteException{
			
			String vmname = "T15-VM";
			ManagedEntity VMe = null;
			try {
				VMe = new InventoryNavigator(si.getRootFolder()).searchManagedEntity("VirtualMachine", vmname);
			} catch (InvalidProperty e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RuntimeFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(VMe == null)
			{
			System.out.println("Virtual Machine "+ vmname + " cannot be found.");
			si.getServerConnection().logout();
			return null;
			}
			
			
			VirtualMachine host = null;
			try {
				host = (VirtualMachine) new InventoryNavigator(
				        si.getRootFolder()).searchManagedEntity(
				        "VirtualMachine", vmname);
			} catch (InvalidProperty e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RuntimeFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PerformanceManager perfMgr = si.getPerformanceManager();
			PerfProviderSummary summary = perfMgr.queryPerfProviderSummary(host);
            int perfInterval = summary.getRefreshRate();            
            PerfMetricId[] queryAvailablePerfMetric = perfMgr.queryAvailablePerfMetric(host, null, null, perfInterval);
            
            PerfQuerySpec qSpec = new PerfQuerySpec();
            qSpec.setEntity(host.getMOR());
            
            qSpec.setMaxSample(1);
            qSpec.setMetricId(queryAvailablePerfMetric);

            qSpec.intervalId = perfInterval;
            PerfEntityMetricBase[] pembs = perfMgr.queryPerf(new PerfQuerySpec[] { qSpec });
            
            for (int i = 0; pembs != null && i < pembs.length; i++) {
            	  
                PerfEntityMetricBase val = pembs[i];
                PerfEntityMetric pem = (PerfEntityMetric) val;
                PerfMetricSeries[] vals = pem.getValue();
                //PerfSampleInfo[] infos = pem.getSampleInfo();

                for (int j = 0; vals != null && j < vals.length; ++j) {
                    PerfMetricIntSeries val1 = (PerfMetricIntSeries) vals[j];


                    long[] longs = val1.getValue();

                    if (val1.getId().getCounterId() == 6)
                    	return new Double(longs[0]);

                    
                }
            }
            
            si.getServerConnection().logout();
            return null;		
			
		}
		
		
		
		
		
		public void getAllCounters(){
			
			PerformanceManager perfMgr = si.getPerformanceManager();
			
			PerfCounterInfo[] pcis = perfMgr.getPerfCounter();
			
			for(int i=0; pcis!=null && i<pcis.length; i++)
			{
			System.out.println("\nKey:" + pcis[i].getKey());
			String perfCounter = pcis[i].getGroupInfo().getKey() + "."
			+ pcis[i].getNameInfo().getKey() + "."
			+ pcis[i].getRollupType();
			System.out.println("PerfCounter:" + perfCounter);
			System.out.println("Level:" + pcis[i].getLevel());
			System.out.println("StatsType:" + pcis[i].getStatsType());
			System.out.println("UnitInfo:"
			+ pcis[i].getUnitInfo().getKey());
			}
		}
		
		
		public void getComputername() throws UnknownHostException{


			String computername=InetAddress.getLocalHost().getHostName();
			System.out.println(computername);


		}
		
}

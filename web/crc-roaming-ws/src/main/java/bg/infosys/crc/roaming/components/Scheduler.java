package bg.infosys.crc.roaming.components;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import bg.infosys.crc.entities.pub.ReportedRoaming;
import bg.infosys.crc.roaming.services.web.BackgroundService;
import bg.infosys.crc.roaming.services.web.OpenDataService;
import bg.infosys.crc.roaming.services.web.ReportService;
import bg.infosys.crc.roaming.workers.SetProvinceTask;

@Component
public class Scheduler implements ServletContextListener {
	private ExecutorService executor;
	private final boolean execute;
	
	@Autowired private BackgroundService		backgroundService;
	@Autowired private OpenDataService			openDataService;
	@Autowired private ReportService			reportService;

	public Scheduler() {
		this.executor = Executors.newFixedThreadPool(5);
		execute = Boolean.parseBoolean(Properties.get("scheduler.enabled"));
	}

	public void addTask(Runnable task) {
		executor.execute(task);
	}

	@Scheduled(cron = "25 3/10 * * * *")
	public void updateMissedProvinces() {
		List<ReportedRoaming> rrList = reportService.getAllMissingProvince();
		for (ReportedRoaming rr : rrList) {
			addTask(new SetProvinceTask(rr, backgroundService));
		}
    }
	
//	@Scheduled(cron = "0 * * * * *")
	@Scheduled(cron = "0 15 1 * * MON")
	public void submitToOpenData() {
		if (execute) {
			openDataService.generateOpenData();
		}
	}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		executor.shutdown();
	}

}

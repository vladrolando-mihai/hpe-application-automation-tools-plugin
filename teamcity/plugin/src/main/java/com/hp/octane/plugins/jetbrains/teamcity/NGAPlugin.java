package com.hp.octane.plugins.jetbrains.teamcity;

/**
 * Created by lazara on 23/12/2015.
 */

import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.plugins.jetbrains.teamcity.actions.ConfigurationActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.actions.GenericNGAActionsController;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.NGAConfigStructure;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.TCConfigurationService;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerExtension;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class NGAPlugin implements ServerExtension {
	public static final String PLUGIN_NAME = NGAPlugin.class.getSimpleName().toLowerCase();
	private static final Logger logger = Logger.getLogger(NGAPlugin.class.getName());

	@Autowired
	private ProjectManager projectManager;
	@Autowired
	private SBuildServer buildServer;
	@Autowired
	private TeamCityPluginServicesImpl pluginServices;
	@Autowired
	private GenericNGAActionsController genericController;
	@Autowired
	private ConfigurationActionsController configurationController;
	@Autowired
	private TCConfigurationService configurationService;
	@Autowired
	private PluginDescriptor pluginDescriptor;
	@Autowired
	private WebControllerManager webControllerManager;

	//  [YG] TODO: move this config cache to the configuration service
	private NGAConfigStructure config;

	@PostConstruct
	private void initPlugin() {
		logger.info("Init HPE NGA CI Plugin");
		buildServer.registerExtension(ServerExtension.class, PLUGIN_NAME, this);
		registerControllers();
		config = configurationService.readConfig();

		ensureServerInstanceID();
		OctaneSDK.init(pluginServices, true);
	}

	public ProjectManager getProjectManager() {
		return projectManager;
	}

	public PluginDescriptor getDescriptor() {
		return pluginDescriptor;
	}

	private void registerControllers() {
		webControllerManager.registerController("/nga/**", genericController);
		webControllerManager.registerController("/octane-rest/**", configurationController);
	}

	private void ensureServerInstanceID() {
		String identity = config.getIdentity();
		if (identity == null || identity.equals("")) {
			config.setIdentity(UUID.randomUUID().toString());
			config.setIdentityFrom(String.valueOf(new Date().getTime()));
			configurationService.saveConfig(config);
		}
	}

	public NGAConfigStructure getConfig() {
		return config;
	}
}

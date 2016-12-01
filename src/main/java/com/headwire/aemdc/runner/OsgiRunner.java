package com.headwire.aemdc.runner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.headwire.aemdc.command.CommandMenu;
import com.headwire.aemdc.command.CreateFileCommand;
import com.headwire.aemdc.command.ReplacePlaceHoldersCommand;
import com.headwire.aemdc.companion.Constants;
import com.headwire.aemdc.companion.Resource;
import com.headwire.aemdc.replacer.OsgiReplacer;
import com.headwire.aemdc.replacer.Replacer;
import com.headwire.aemdc.util.ConfigUtil;


/**
 * OSGI creator
 *
 */
public class OsgiRunner extends BasisRunner {

  private static final Logger LOG = LoggerFactory.getLogger(OsgiRunner.class);
  private static final String HELP_FOLDER = "osgi";

  /**
   * Invoker
   */
  private final CommandMenu menu = new CommandMenu();
  private final Resource resource;
  private final Properties configProps;

  /**
   * Constructor
   *
   * @param resource
   *          - resource object
   * @throws IOException
   *           - IOException
   */
  public OsgiRunner(final Resource resource) throws IOException {
    this.resource = resource;

    // Get Config Properties from config file
    configProps = ConfigUtil.getConfigProperties();

    LOG.debug("OSGI runner starting...");

    resource.setSourceFolderPath(configProps.getProperty(Constants.CONFIGPROP_SOURCE_OSGI_FOLDER));
    resource.setTargetFolderPath(configProps.getProperty(Constants.CONFIGPROP_TARGET_OSGI_FOLDER));

    // set target runmode folder
    final Map<String, String> commonJcrProps = resource.getJcrPropsSet(Constants.PLACEHOLDER_PROPS_SET_COMMON);
    if (commonJcrProps != null) {
      final String runmode = commonJcrProps.get(Constants.PLACEHOLDER_RUNMODE);
      if (StringUtils.isNotBlank(runmode)) {
        // add config.<runmode> to the target path
        resource
            .setTargetFolderPath(configProps.getProperty(Constants.CONFIGPROP_TARGET_OSGI_FOLDER) + "." + runmode);
      }
    }

    // Set global config properties in the resource
    setGlobalConfigProperties(configProps, resource);

    // Creates Invoker object, command object and configure them
    menu.setCommand("CreateFile", new CreateFileCommand(resource));
    menu.setCommand("ReplacePlaceHolders", new ReplacePlaceHoldersCommand(resource, getPlaceHolderReplacer()));
  }

  /**
   * Run commands
   *
   * @throws IOException
   */
  @Override
  protected void run() throws IOException {
    // Invoker invokes command
    menu.runCommand("CreateFile");
    menu.runCommand("ReplacePlaceHolders");
  }

  @Override
  public String getHelpFolder() {
    return HELP_FOLDER;
  }

  @Override
  public String getSourceFolder() {
    return resource.getSourceFolderPath();
  }

  @Override
  public Collection<File> listAvailableTemplates(final File dir) {
    final Collection<File> fileList = FileUtils.listFiles(dir, new String[] { Constants.FILE_EXT_XML }, false);
    return fileList;
  }

  @Override
  public boolean checkConfiguration() {
    final String targetPath = resource.getTargetFolderPath();
    // get target project jcr path
    final String targetProjectRoot = configProps.getProperty(Constants.CONFIGPROP_TARGET_PROJECT_ROOT);
    final int pos = targetPath.indexOf(targetProjectRoot);
    if (pos == -1) {
      LOG.error("The target project root jcr path {} is different to target path {} in the config file.",
          Constants.CONFIGPROP_TARGET_PROJECT_ROOT, targetPath);
      return false;
    }
    return true;
  }

  @Override
  public Replacer getPlaceHolderReplacer() {
    return new OsgiReplacer(resource);
  }

}

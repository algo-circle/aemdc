package com.headwire.aemdc.runner;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.headwire.aemdc.command.CommandMenu;
import com.headwire.aemdc.command.CreateDirCommand;
import com.headwire.aemdc.command.ReplacePathPlaceHoldersCommand;
import com.headwire.aemdc.command.ReplacePlaceHoldersCommand;
import com.headwire.aemdc.companion.Constants;
import com.headwire.aemdc.companion.Resource;
import com.headwire.aemdc.replacer.ComponentReplacer;
import com.headwire.aemdc.replacer.Replacer;
import com.headwire.aemdc.util.ConfigUtil;


/**
 * Component creator
 *
 */
public class ComponentRunner extends BasisRunner {

  private static final Logger LOG = LoggerFactory.getLogger(ComponentRunner.class);
  private static final String HELP_FOLDER = "component";

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
  public ComponentRunner(final Resource resource) throws IOException {
    this.resource = resource;

    // Get Config Properties from config file
    configProps = ConfigUtil.getConfigProperties();

    LOG.debug("Component runner starting...");

    resource.setSourceFolderPath(configProps.getProperty(Constants.CONFIGPROP_SOURCE_COMPONENTS_FOLDER));
    resource.setTargetFolderPath(configProps.getProperty(Constants.CONFIGPROP_TARGET_COMPONENTS_FOLDER));

    // Set global config properties in the resource
    setGlobalConfigProperties(configProps, resource);

    // Creates Invoker object, command object and configure them
    menu.setCommand("CreateDir", new CreateDirCommand(resource));
    menu.setCommand("ReplacePlaceHolders", new ReplacePlaceHoldersCommand(resource, getPlaceHolderReplacer()));
    menu.setCommand("ReplacePathPlaceHolders", new ReplacePathPlaceHoldersCommand(resource, getPlaceHolderReplacer()));
  }

  /**
   * Run commands
   *
   * @throws IOException
   */
  @Override
  protected void run() throws IOException {
    // Invoker invokes command
    menu.runCommand("CreateDir");
    menu.runCommand("ReplacePlaceHolders");
    menu.runCommand("ReplacePathPlaceHolders");
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
    final Collection<File> fileList = listRootDirs(dir);
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
    return new ComponentReplacer(resource);
  }
}

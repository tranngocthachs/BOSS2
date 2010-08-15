package uk.ac.warwick.dcs.boss.plugins.spi.extralinks;

import uk.ac.warwick.dcs.boss.plugins.PluginEntryLinkProvider;

public interface StudentSubmissionPluginEntryProvider extends PluginEntryLinkProvider {
	public String getSubmissionParaString();
}

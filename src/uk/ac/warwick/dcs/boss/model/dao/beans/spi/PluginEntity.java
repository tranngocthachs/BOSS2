package uk.ac.warwick.dcs.boss.model.dao.beans.spi;

import uk.ac.warwick.dcs.boss.model.dao.beans.Entity;
import uk.ac.warwick.dcs.boss.model.dao.spi.IPluginEntityDAO;


public abstract class PluginEntity extends Entity {
	public abstract Class<? extends IPluginEntityDAO<? extends PluginEntity>> getAssiociatedDAO();
}

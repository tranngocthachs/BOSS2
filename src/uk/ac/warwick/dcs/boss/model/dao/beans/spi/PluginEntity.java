package uk.ac.warwick.dcs.boss.model.dao.beans.spi;

import uk.ac.warwick.dcs.boss.model.dao.IEntityDAO;
import uk.ac.warwick.dcs.boss.model.dao.beans.Entity;

public abstract class PluginEntity extends Entity {
	public abstract Class<? extends IEntityDAO<? extends PluginEntity>> getAssiociatedDAO();
}

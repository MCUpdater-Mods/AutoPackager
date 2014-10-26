package org.mcupdater.autopackager;

import com.dynious.refinedrelocation.api.APIUtils;
import com.dynious.refinedrelocation.api.tileentity.ISortingMember;
import com.dynious.refinedrelocation.api.tileentity.handlers.ISortingMemberHandler;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "com.dynious.refinedrelocation.api.tileentity.ISortingMember", modid = "RefinedRelocation")
public class TileSortingPackager extends TilePackager implements ISortingMember
{
    private boolean isFirstTick = true;
    private Object sortingHandler;

    public TileSortingPackager() {
		super();
	}

	@Override
	public void updateEntity() {
        if (isFirstTick) {
            getHandler().onTileAdded();
            isFirstTick = false;
        }
		super.updateEntity();
	}

    @Optional.Method(modid = "RefinedRelocation")
    @Override
    public ISortingMemberHandler getHandler() {
        if (sortingHandler == null) {
            sortingHandler = APIUtils.createSortingMemberHandler(this);
        }
        return (ISortingMemberHandler) sortingHandler;
    }
}

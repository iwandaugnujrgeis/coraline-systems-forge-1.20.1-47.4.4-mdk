// net/zharok01/coralinesystems/client/portal/PortalTransitionContext.java
package net.zharok01.coralinesystems.client.portal;

import com.legacy.structure_gel.api.block.GelPortalBlock;
import javax.annotation.Nullable;

public final class PortalTransitionContext
{
    @Nullable
    private static GelPortalBlock pendingPortal = null;
    private static boolean netherPortalPending  = false;

    private PortalTransitionContext() {}

    public static void set(@Nullable GelPortalBlock portal)
    {
        pendingPortal = portal;
        netherPortalPending = false;
    }

    /** Called when a Nether portal transition is detected but no GelPortalBlock exists. */
    public static void setNetherPortal()
    {
        pendingPortal = null;
        netherPortalPending = true;
    }

    public static void clear()
    {
        pendingPortal = null;
        netherPortalPending = false;
    }

    /** True if ANY portal transition is pending (Gel or Nether). */
    public static boolean hasTransition()
    {
        return pendingPortal != null || netherPortalPending;
    }

    @Nullable
    public static GelPortalBlock peekPortal()
    {
        return pendingPortal;
    }

    @Nullable
    public static GelPortalBlock consumeAndClear()
    {
        GelPortalBlock result = pendingPortal;
        pendingPortal = null;
        netherPortalPending = false;
        return result;
    }
}
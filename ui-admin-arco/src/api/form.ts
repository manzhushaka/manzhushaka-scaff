import type { HttpResponse } from './interceptor';

export interface BaseInfoModel {
  activityName: string;
  channelType: string;
  promotionTime: string[];
  promoteLink: string;
}

export interface ChannelInfoModel {
  advertisingSource: string;
  advertisingMedia: string;
  keyword: string[];
  pushNotify: boolean;
  advertisingContent: string;
}

export type UnitChannelModel = BaseInfoModel & ChannelInfoModel;

/**
 * 完成分步表单的前端提交状态。
 *
 * Java 管理端没有营销活动实体或对应 Controller，因此该示例页只负责
 * Arco 表单校验和步骤流转，不伪造一个无业务含义的后端写入接口。
 */
export function submitChannelForm(data: UnitChannelModel): Promise<HttpResponse<UnitChannelModel>> {
  return Promise.resolve({ code: 200, msg: '表单校验通过', data });
}

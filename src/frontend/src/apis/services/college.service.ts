import { publicClient } from '../client'
import { ResponseMessage, CollegeDTO } from '../schema/type'

export const collegeService = {
  async getColleges(): Promise<ResponseMessage<CollegeDTO[]>> {
    const response = await publicClient.get<ResponseMessage<CollegeDTO[]>>('/colleges')
    return response.data
  },
}
